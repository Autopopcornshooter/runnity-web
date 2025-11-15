const openScheduleCheckBtn = document.getElementById("openScheduleCheckBtn");
const title = $("joinTitle");
const date = $("joinDate");
const desc = $("joinDesc");
const joinLocation = $("joinLocation");
const creator = $("creator");

openScheduleCheckBtn.addEventListener("click", () => {

})

async function loadRecentSchedule() {
  console.log("일정 체크 모달");
  const roomId = document.getElementById("currentRoomId");
  try {
    const response = await fetch(`/chat-rooms/${roomId}/schedules/recent`, {
      method: "GET"
    });
    if (!res.ok) {
      const message = await res.text();
      throw new Error("일정 불러오기 실패 : " + message);
    }
    // TODO
    const data = await response.json();
    openScheduleModal(data);

  } catch (err) {
    console.error("일정 불러오기 실패 : " + err);
    alert("일정을 불러올 수 없습니다");
  }
}

function fillData(data) {
  title.textContent = data.title;
  desc.textContent = data.detail;
  const dateStr = data.startAt.replace("T", " ").slice(0, 16);
  date.textContent = `일시: ${dateStr}`;
  creator.value = data.scheduleCreatorId;

}

function openScheduleModal(data) {
  fillData(data);
  //TODO

}