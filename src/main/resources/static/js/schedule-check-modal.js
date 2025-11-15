const openScheduleCheckBtn = document.getElementById("openScheduleCheckBtn");
const scheduleJoinModal = document.getElementById("scheduleJoinModal");
const title = document.getElementById("joinTitle");
const date = document.getElementById("joinDate");
const desc = document.getElementById("joinDesc");
const joinLocation = document.getElementById("joinLocation");
const creator = document.getElementById("creator");
const closeScheduleCheckBtn = document.getElementById("joinCloseBtn");

openScheduleCheckBtn.addEventListener("click", () => {
    openModal();
})
closeScheduleCheckBtn.addEventListener("click", () => {
    closeModal();
})
if (scheduleJoinModal) {
    scheduleJoinModal.addEventListener("click", (e) => {
        if (e.target === scheduleJoinModal) {
            closeModal();
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

    } catch (err) {
        console.error("일정 불러오기 실패 : " + err);
        alert("일정을 불러올 수 없습니다");
    }
}

function fillData(data) {
    title.textContent = data.title ?? '';
    desc.textContent = data.detail ?? '';
    const dateStr = data.startAt.replace("T", " ").slice(0, 16);
    date.textContent = `일시: ${dateStr}`;
    creator.value = data.scheduleCreatorId;

}

function openModal() {
    loadRecentSchedule();
    //TODO
    scheduleJoinModal.style.display = "flex";
}

function closeModal() {
    scheduleJoinModal.style.display = "none";
}