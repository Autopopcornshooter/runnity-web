const openScheduleCheckBtn = document.getElementById("openScheduleCheckBtn");
const closeScheduleCheckBtn = document.getElementById("joinCloseBtn");
const scheduleJoinModal = document.getElementById("scheduleJoinModal");
const title = document.getElementById("joinTitle");
const date = document.getElementById("joinDate");
const desc = document.getElementById("joinDesc");
const joinLocation = document.getElementById("joinLocation");
const creator = document.getElementById("creator");

const joinBtn = document.getElementById("joinYesBtn");
const declineBtn = document.getElementById("joinNoBtn");

openScheduleCheckBtn.addEventListener("click", () => {
  openJoinModal();
})
closeScheduleCheckBtn.addEventListener("click", () => {
  closeJoinModal();
})
if (scheduleJoinModal) {
  scheduleJoinModal.addEventListener("click", (e) => {
    if (e.target === scheduleJoinModal) {
      closeJoinModal();
    }
  })
}

async function loadRecentSchedule() {
  console.log("일정 체크 모달");
  const roomId = document.getElementById("currentRoomId").value;
  try {
    const response = await fetch(`/chat-rooms/${roomId}/schedules/recent`, {
      method: "GET"
    });
    if (!response.ok) {
      const message = await response.text();
      throw new Error("일정 불러오기 실패 : " + message);
    }
    // TODO
    const data = await response.json();
    fillData(data);
    openScheduleCheckBtn.style.display = "flex";

  } catch (err) {
    console.log("일정 없음 : " + err);
    openScheduleCheckBtn.style.display = "none";
  }
}

function fillData(data) {
  title.textContent = data.title ?? '';
  desc.textContent = data.detail ?? '';
  const dateStr = data.startAt.replace("T", " ").slice(0, 16);
  date.textContent = `일시: ${dateStr}`;
  creator.value = data.scheduleCreatorId;

}

function openJoinModal() {
  // loadRecentSchedule();
  //TODO
  scheduleJoinModal.style.display = "flex";
}

function closeJoinModal() {
  scheduleJoinModal.style.display = "none";
}

window.addEventListener('room:active', (e) => {
  loadRecentSchedule();
})