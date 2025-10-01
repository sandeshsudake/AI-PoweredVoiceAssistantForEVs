const videoElement = document.getElementById('inputVideo');
const canvasElement = document.getElementById('outputCanvas');
const canvasCtx = canvasElement.getContext('2d');
const statusText = document.getElementById('status');
const earDisplay = document.getElementById('earDisplay');
const alarm = document.getElementById('alarmSound');

let eyeClosedFrames = 0;
const EYE_CLOSED_THRESHOLD = 0.18;
const DROWSY_FRAMES_THRESHOLD = 10;
let earHistory = [];
let drowsySpoken = false;

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

const faceMesh = new FaceMesh({locateFile: (file) => {
  return `https://cdn.jsdelivr.net/npm/@mediapipe/face_mesh/${file}`;
}});

faceMesh.setOptions({
  maxNumFaces: 1,
  refineLandmarks: true,
  minDetectionConfidence: 0.5,
  minTrackingConfidence: 0.5
});

faceMesh.onResults(results => {
  canvasCtx.save();
  canvasCtx.clearRect(0, 0, canvasElement.width, canvasElement.height);
  canvasCtx.drawImage(results.image, 0, 0, canvasElement.width, canvasElement.height);

  if (results.multiFaceLandmarks && results.multiFaceLandmarks.length > 0) {
    const landmarks = results.multiFaceLandmarks[0];

    if (isEyeClosed(landmarks)) {
      eyeClosedFrames++;
      if (eyeClosedFrames >= DROWSY_FRAMES_THRESHOLD) {
        statusText.innerText = "⚠️ DROWSINESS DETECTED! WAKE UP!";
        statusText.style.color = "red";
        statusText.style.fontSize = "24px";
        statusText.style.fontWeight = "bold";
        if (alarm.paused) {
          alarm.loop = true;
          alarm.play();
        }
        speakDrowsyWarning();
      } else {
        statusText.innerText = `⚠️ Eyes Closed: ${eyeClosedFrames}/${DROWSY_FRAMES_THRESHOLD}`;
        statusText.style.color = "orange";
        statusText.style.fontSize = "18px";
        statusText.style.fontWeight = "bold";
        drowsySpoken = false;
      }
    } else {
      if (eyeClosedFrames > 0) {
        console.log(`Eyes opened after ${eyeClosedFrames} frames`);
      }
      eyeClosedFrames = 0;
      statusText.innerText = "✓ Status: Eyes Open - Alert";
      statusText.style.color = "green";
      statusText.style.fontSize = "16px";
      statusText.style.fontWeight = "normal";
      alarm.pause();
      alarm.loop = false;
      alarm.currentTime = 0;
      drowsySpoken = false;
    }
  }
  canvasCtx.restore();
});

const camera = new Camera(videoElement, {
  onFrame: async () => {
    await faceMesh.send({image: videoElement});
  },
  width: 400,
  height: 300
});
camera.start();