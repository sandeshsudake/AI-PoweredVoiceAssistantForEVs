 // --- THEME TOGGLE ---
    const themeSwitch = document.getElementById('themeSwitch');
    const userPref = localStorage.getItem('ev-va-theme');
    if (userPref === 'dark') document.body.classList.add('dark');
    if (userPref === 'dark') themeSwitch.checked = false;
    if (userPref === 'light') themeSwitch.checked = true;
    themeSwitch.checked = !document.body.classList.contains('dark');
    themeSwitch.onchange = function() {
      if (themeSwitch.checked) {
        document.body.classList.remove('dark');
        localStorage.setItem('ev-va-theme', 'light');
      } else {
        document.body.classList.add('dark');
        localStorage.setItem('ev-va-theme', 'dark');
      }
    };
    // --- MAIN LOGIC ---
    const micBtn = document.getElementById('micBtn');
    const responseDiv = document.getElementById('responseDiv');
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    const speechSynthesis = window.speechSynthesis;
    let lastUtterance = null;
    function speakText(text) {
      if (!speechSynthesis) return;
      if (speechSynthesis.speaking) { speechSynthesis.cancel(); }
      lastUtterance = new SpeechSynthesisUtterance(text);
      lastUtterance.lang = 'en-US';
      lastUtterance.pitch = 1.1;
      lastUtterance.rate = 1.05;
      speechSynthesis.speak(lastUtterance);
    }
    function interruptResponse() {
      if (speechSynthesis && speechSynthesis.speaking) speechSynthesis.cancel();
    }
    if (!SpeechRecognition) {
      responseDiv.textContent = 'Your browser does not support Speech Recognition.';
      responseDiv.classList.add('visible');
      micBtn.disabled = true;
    } else {
      const recognition = new SpeechRecognition();
      recognition.lang = 'en-US';
      recognition.interimResults = false;
      micBtn.addEventListener('click', () => {
        interruptResponse();
        try {
          recognition.start();
          micBtn.classList.add('listening');
          showProcessingMessage('Listening... Please speak your command.');
        } catch(e) {
          showResponse('Error: Could not start recognition.');
        }
      });
      recognition.onresult = e => {
        micBtn.classList.remove('listening');
        const transcript = e.results[0][0].transcript;
        showProcessingMessage('Processing your command');
        fetch('/api/voice-command', {
          method: 'POST',
          headers: {'Content-Type': 'application/json'},
          body: JSON.stringify({ text: transcript })
        })
        .then(res => { if (!res.ok) throw new Error('Network response was not ok'); return res.json(); })
        .then(data => {
          const reply = data.reply || 'Sorry, no response from assistant.';
          showResponse(reply);
          speakText(reply);
        })
        .catch(err => {
          showResponse('Error: ' + err.message);
        });
      };
      recognition.onerror = e => {
        micBtn.classList.remove('listening');
        showResponse('Speech recognition error: ' + e.error);
      };
      recognition.onend = () => {
        micBtn.classList.remove('listening');
      };
    }
    function showProcessingMessage(msg) {
      responseDiv.innerHTML = `${msg}
        <span class="loader-dots" aria-hidden="true">
          <span></span><span></span><span></span>
        </span>`;
      responseDiv.classList.add('visible');
    }
    function showResponse(text) {
      const urlRegex = /(https?:\/\/[^\s]+)/g;
      const urls = text.match(urlRegex);
      let html = text.replace(urlRegex, url =>
        `<a href="${url}" target="_blank" rel="noopener noreferrer">${url}</a>`
      );

      if (urls && urls.length) {
        for (const url of urls) {
          if (url.includes('google.com/maps/dir/')) {
            const path = url.split('/maps/dir/')[1];
            if (path) {
              const [origin, destination] = path.split('/');
              if (origin && destination) {
                const queryStr = encodeURIComponent(origin + ' to ' + destination);
                const embedUrl = `https://www.google.com/maps?q=${queryStr}&output=embed`;
                html += `<iframe width="320" height="220" style="border-radius:15px; border:none; box-shadow:0 2px 17px rgba(0,0,0,0.13)"
                  src="${embedUrl}" allowfullscreen loading="lazy"></iframe>`;
                break;
              }
            }
          }
        }
      }

      responseDiv.innerHTML = html;
      responseDiv.classList.add('visible');
    }

