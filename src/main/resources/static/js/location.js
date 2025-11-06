fetch('/api/alan/chat/location')
    .then(res => res.json())
    .then(dataList => {
        const container = document.getElementById("location-content");
        container.innerHTML = ""; // 초기화

        if (!Array.isArray(dataList) || dataList.length === 0) {
            container.innerText = "러닝 코스 정보를 불러올 수 없습니다.";
            return;
        }

        // 리스트 생성
        const ul = document.createElement("ul");
        ul.classList.add("course-list");

        dataList.forEach(data => {
            // null 제거 및 기본값 처리
            if (!data.courseName && !data.courseInfo) return;

            const li = document.createElement("li");
            li.classList.add("course-item");
            li.innerHTML = `
                <strong>${data.courseName || "이름 없음"}</strong><br>
                🏃 코스 길이: ${data.courseLength || "정보 없음"}<br>
                👍 평가 별점: ${data.recommend || "정보 없음"}<br>
                🛣️ 코스 소개: ${data.courseInfo || "소개 없음"}
            `.trim();
            ul.appendChild(li);
        });

        container.appendChild(ul);
    })
    .catch(err => {
        document.getElementById('location-content').innerText = "AI 응답 불가";
        console.error(err);
    });