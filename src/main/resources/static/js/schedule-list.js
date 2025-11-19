// --- 요소 ---
const scheduleListModal = document.getElementById("scheduleListModal");
const scheduleListContainer = document.getElementById("scheduleListContainer");
const scheduleListCloseBtn = document.getElementById("scheduleListCloseBtn");

// 일정 목록 버튼 (페이지에 이 ID가 있어야 함)
const openScheduleListBtn = document.getElementById("openScheduleListBtn");

// 일정 상세 모달 함수 (너의 기존 함수 계속 사용)
function openJoinModal() {
  document.getElementById("scheduleJoinModal").style.display = "flex";
}

// --------------------------------------------
// 일정 목록 모달 열기
// --------------------------------------------
openScheduleListBtn.addEventListener("click", () => {
  scheduleListModal.style.display = "flex";
  loadScheduleList();
});

// 닫기
scheduleListCloseBtn.addEventListener("click", () => {
  scheduleListModal.style.display = "none";
});

// 모달 외부 클릭 → 닫기
scheduleListModal.addEventListener("click", (e) => {
  if (e.target === scheduleListModal) {
    scheduleListModal.style.display = "none";
  }
});

// --------------------------------------------
// 일정 목록 불러오기
// --------------------------------------------
async function loadScheduleList() {
  const roomId = document.getElementById("currentRoomId").value;

  try {
    const response = await fetch(`/chat-rooms/${roomId}/schedules`);
    if (!response.ok) {
      throw new Error();
    }

    const list = await response.json();
    renderScheduleList(list);
  } catch {
    scheduleListContainer.innerHTML = "<p>일정 없음</p>";
  }
}

// --------------------------------------------
// 일정 목록 표시
// --------------------------------------------
function renderScheduleList(list) {
  scheduleListContainer.innerHTML = "";

  list.forEach(item => {
    const div = document.createElement("div");
    div.classList.add("schedule-item");

    const dateStr = item.startAt.replace("T", " ").slice(0, 16);

    div.innerHTML = `
      <strong>${item.title}</strong><br>
      <span>${dateStr}</span>
    `;

    div.addEventListener("click", () => {
      scheduleListModal.style.display = "none";
      openScheduleDetail(item.scheduleId);
    });

    scheduleListContainer.appendChild(div);
  });
}

// --------------------------------------------
// 일정 상세 정보 재조회 후 기존 모달로 표시
// --------------------------------------------
async function openScheduleDetail(scheduleId) {
  const roomId = document.getElementById("currentRoomId").value;

  try {
    const res = await fetch(`/chat-rooms/${roomId}/schedules/${scheduleId}`);
    const data = await res.json();
    
    fillScheduleData(data);
    openJoinModal();
  } catch (err) {
    console.error(err);
  }
}