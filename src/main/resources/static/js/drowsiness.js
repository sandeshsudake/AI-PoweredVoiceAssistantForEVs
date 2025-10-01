const videoElement = document.getElementById('inputVideo');
const canvasElement = document.getElementById('outputCanvas');
const canvasCtx = canvasElement.getContext('2d');
const statusText = document.getElementById('status');
const earDisplay = document.getElementById('earDisplay');
const alarm = document.getElementById('alarmSound');
// ADDED: Hijack status element
const hijackStatusText = document.getElementById('hijackStatus');

// --- Drowsiness variables ---
let eyeClosedFrames = 0;
const EYE_CLOSED_THRESHOLD = 0.18;
const DROWSY_FRAMES_THRESHOLD = 10;
let earHistory = [];
let drowsySpoken = false;

// --- Hijack detection variables ---
let handRaisedFrames = 0;
const HAND_RAISED_FRAMES_THRESHOLD = 60; // 2 seconds at 30fps (adjust as needed)
let latestFaceLandmarks = null;
let hijackAlertSent = false;
let handNotDetectedGrace = 0; // Grace period counter
const HAND_NOT_DETECTED_GRACE_MAX = 5; // Allow up to 5 missed frames before decrementing

function getEuclideanDistance(point1, point2) {
  const dx = point1.x - point2.x;
  const dy = point1.y - point2.y;
  const dz = (point1.z || 0) - (point2.z || 0);
  return Math.sqrt(dx * dx + dy * dy + dz * dz);
}

function calculateEAR(landmarks, eyePoints) {
  const P1 = landmarks[eyePoints[0]];
  const P2 = landmarks[eyePoints[1]];
  const P3 = landmarks[eyePoints[2]];
  const P4 = landmarks[eyePoints[3]];
  const P5 = landmarks[eyePoints[4]];
  const P6 = landmarks[eyePoints[5]];

  const vertical1 = getEuclideanDistance(P3, P4);
  const vertical2 = getEuclideanDistance(P5, P6);
  const horizontal = getEuclideanDistance(P1, P2);

  const ear = (vertical1 + vertical2) / (2.0 * horizontal);
  return ear;
}

function isEyeClosed(landmarks) {
  const leftEyePoints = [33, 133, 159, 145, 160, 144];
  const rightEyePoints = [362, 263, 386, 374, 387, 373];

  const leftEAR = calculateEAR(landmarks, leftEyePoints);
  const rightEAR = calculateEAR(landmarks, rightEyePoints);
  const avgEAR = (leftEAR + rightEAR) / 2.0;

  earHistory.push(avgEAR);
  if (earHistory.length > 100) earHistory.shift();

  const status = avgEAR < EYE_CLOSED_THRESHOLD ? 'CLOSED' : 'OPEN';
  earDisplay.innerHTML = `EAR: ${avgEAR.toFixed(3)} (Threshold: ${EYE_CLOSED_THRESHOLD}) - Eyes: <strong>${status}</strong>`;
  earDisplay.style.color = avgEAR < EYE_CLOSED_THRESHOLD ? 'red' : 'green';

  return avgEAR < EYE_CLOSED_THRESHOLD;
}

function speakDrowsyWarning() {
  if (window.speechSynthesis && !drowsySpoken) {
    const utter = new SpeechSynthesisUtterance("Drowsiness detected! Please stay alert and take a break if needed.");
    utter.lang = "en-US";
    utter.rate = 1.0;
    utter.pitch = 1.1;
    window.speechSynthesis.speak(utter);
    drowsySpoken = true;
  }
}

// ADDED: Hijack Alert Function (Web3Forms)
function sendHijackAlertEmail() {
    if (hijackAlertSent) return; // Prevent multiple alerts
    console.log("Sending hijack alert email via Web3Forms...");
    hijackAlertSent = true;
    hijackStatusText.innerText = "⏳ Getting location...";
    hijackStatusText.style.color = "orange";

    // Replace with your Web3Forms access key and recipient email
    const accessKey = "f05b882e-ff8c-485d-b1fd-0699665c638a";
    const emailTo = "sandeshsudake818@gmail.com";

    function sendEmailWithMessage(message) {
        const formData = new FormData();
        formData.append("access_key", accessKey);
        formData.append("subject", "Hijack Alert Detected!");
        formData.append("from_name", "EV Voice Assistant");
        formData.append("email", emailTo);
        formData.append("message", message);

        fetch("https://api.web3forms.com/submit", {
            method: "POST",
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                console.log("Hijack alert email sent via Web3Forms.");
                hijackStatusText.innerText = "🆘 ALERT SENT!";
                hijackStatusText.style.color = "red";
            } else {
                console.error("Failed to send hijack alert via Web3Forms.", data);
                hijackStatusText.innerText = "❌ ALERT FAILED!";
                hijackStatusText.style.color = "gray";
                hijackAlertSent = false; // Allow retrying if it failed
            }
        })
        .catch(error => {
            console.error('Error sending hijack alert via Web3Forms:', error);
            hijackStatusText.innerText = "❌ ALERT FAILED!";
            hijackStatusText.style.color = "gray";
            hijackAlertSent = false; // Allow retrying if it failed
        });
    }

    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            function(position) {
                const lat = position.coords.latitude.toFixed(6);
                const lon = position.coords.longitude.toFixed(6);
                const accuracy = position.coords.accuracy ? position.coords.accuracy.toFixed(1) : 'unknown';
                const mapsLink = `https://maps.google.com/?q=${lat},${lon}`;
                let message = `Hijack detected from user vehicle.\nCurrent Location: ${lat}, ${lon}\nAccuracy: ${accuracy} meters\nGoogle Maps: ${mapsLink}`;
                console.log("Location for hijack alert:", lat, lon, "Accuracy:", accuracy);
                sendEmailWithMessage(message);
            },
            function(error) {
                const message = `Hijack detected from user vehicle.\nLocation unavailable: ${error.message}`;
                sendEmailWithMessage(message);
            },
            {timeout: 20000, enableHighAccuracy: true, maximumAge: 0}
        );
    } else {
        const message = "Hijack detected from user vehicle. Location unavailable: Geolocation not supported.";
        sendEmailWithMessage(message);
    }
}

// --- MediaPipe FaceMesh Initialization ---
const faceMesh = new FaceMesh({locateFile: (file) => {
  return `https://cdn.jsdelivr.net/npm/@mediapipe/face_mesh/${file}`;
}});
faceMesh.setOptions({
  maxNumFaces: 1,
  refineLandmarks: true,
  minDetectionConfidence: 0.5,
  minTrackingConfidence: 0.5
});

// ADDED: MediaPipe Hands Initialization
const hands = new Hands({locateFile: (file) => {
  return `https://cdn.jsdelivr.net/npm/@mediapipe/hands/${file}`;
}});
hands.setOptions({
  maxNumHands: 1,
  modelComplexity: 1,
  minDetectionConfidence: 0.5,
  minTrackingConfidence: 0.5
});

// --- FaceMesh Results Handler ---
faceMesh.onResults(results => {
  // Store landmarks for hand detection logic
  latestFaceLandmarks = results.multiFaceLandmarks.length ? results.multiFaceLandmarks[0] : null;

  canvasCtx.save();
  canvasCtx.clearRect(0, 0, canvasElement.width, canvasElement.height);
  canvasCtx.drawImage(results.image, 0, 0, canvasElement.width, canvasElement.height);

  if (latestFaceLandmarks) {
    // Drowsiness detection logic
    if (isEyeClosed(latestFaceLandmarks)) {
      eyeClosedFrames++;
      if (eyeClosedFrames >= DROWSY_FRAMES_THRESHOLD) {
        statusText.innerText = "⚠️ DROWSINESS DETECTED! WAKE UP!";
        statusText.style.color = "red";
        if (alarm.paused) {
          alarm.loop = true;
          alarm.play();
        }
        speakDrowsyWarning();
      } else {
        statusText.innerText = `⚠️ Eyes Closed: ${eyeClosedFrames}/${DROWSY_FRAMES_THRESHOLD}`;
        statusText.style.color = "orange";
        drowsySpoken = false;
      }
    } else {
      eyeClosedFrames = 0;
      statusText.innerText = "✓ Status: Eyes Open - Alert";
      statusText.style.color = "green";
      alarm.pause();
      alarm.currentTime = 0;
      drowsySpoken = false;
    }
  }
  canvasCtx.restore();
});

// ADDED: Hands Results Handler
hands.onResults(results => {
    let handIsRaised = false;
    // Check if both hand and face landmarks are available
    if (results.multiHandLandmarks && results.multiHandLandmarks.length > 0 && latestFaceLandmarks) {
        const handLandmarks = results.multiHandLandmarks[0];
        const wrist = handLandmarks[0]; // Wrist landmark
        const nose = latestFaceLandmarks[1]; // Nose landmark from face mesh

        // Check if the wrist is above the nose (lower Y value means higher on screen)
        if (wrist.y < nose.y) {
            handIsRaised = true;
        }
    }

    if (handIsRaised) {
        handRaisedFrames++;
        handNotDetectedGrace = 0; // Reset grace period
        if (handRaisedFrames >= HAND_RAISED_FRAMES_THRESHOLD) {
            if (!hijackAlertSent) {
                hijackStatusText.innerText = "🆘 HIJACK DETECTED! ALERTING...";
                hijackStatusText.style.color = "red";
                sendHijackAlertEmail();
            }
        } else {
            hijackStatusText.innerText = `✋ Hand Raised: ${handRaisedFrames}/${HAND_RAISED_FRAMES_THRESHOLD}`;
            hijackStatusText.style.color = "orange";
        }
    } else {
        // If hand is not detected, allow a grace period before decrementing
        if (handRaisedFrames > 0) {
            if (handNotDetectedGrace < HAND_NOT_DETECTED_GRACE_MAX) {
                handNotDetectedGrace++;
            } else {
                handRaisedFrames = Math.max(0, handRaisedFrames - 1);
                handNotDetectedGrace = 0;
            }
        }
        if (!hijackAlertSent) { // Don't reset status if an alert was already sent and is now locked
            hijackStatusText.innerText = "✓ Status: Secure";
            hijackStatusText.style.color = "green";
        }
    }

    // Draw hand landmarks on top
    canvasCtx.save();
    if (results.multiHandLandmarks) {
        for (const landmarks of results.multiHandLandmarks) {
            drawConnectors(canvasCtx, landmarks, HAND_CONNECTIONS, {color: '#00FF00', lineWidth: 5});
            drawLandmarks(canvasCtx, landmarks, {color: '#FF0000', lineWidth: 2});
        }
    }
    canvasCtx.restore();
});


// UPDATED: Camera Setup to send to both models
const camera = new Camera(videoElement, {
  onFrame: async () => {
    await faceMesh.send({image: videoElement});
    await hands.send({image: videoElement});
  },
  width: 400,
  height: 300
});
camera.start();