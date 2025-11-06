fetch('/api/alan/chat/weather')
    .then(res => res.json())
    .then(data => {
        document.getElementById("weather-content").innerHTML =
            `🌤 날씨: ${data.presentWeather}<br>
                🌡️ 온도: ${data.temperature}<br>
                💧 습도: ${data.humidity}<br>
                ☔️ 강수 확률: ${data.percentage}<br>
                💦 강수량: ${data.precipitation}<br>
                📈 최고/최저 기온: ${data.highLow}<br>
                🌫 미세먼지: ${data.dust}`;

        document.getElementById("comment").innerHTML = `<h3>⏰시간별 날씨</h3>`;
        // 시간별 요약
        const list = document.getElementById("hourly-list");
        list.innerHTML = "";

        data.hourlyList.forEach(item => {
            const li = document.createElement("li");
            li.innerText = `${item.hourly} : 기온 ${item.temperature}, 강수확률 ${item.percentage}`;
            list.appendChild(li);
        });

        document.getElementById("weather-summary").innerHTML = `<h3>📝한 줄 요약</h3> ${data.weatherSummary}`;

        // document.getElementById('weather-content').innerText = data.weather || "응답 없음";

    })
    .catch(err => {
        document.getElementById('weather-content').innerText = "AI 응답 불가";
        console.error(err);
    });