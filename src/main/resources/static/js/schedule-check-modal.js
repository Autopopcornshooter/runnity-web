const scCloseBtn = document.getElementById("joinCloseBtn");
const scModal = document.getElementById("scheduleJoinModal");

const scTitle = document.getElementById("joinTitle");
const scDate = document.getElementById("joinDate");
const scDesc = document.getElementById("joinDesc");
const scLocation = document.getElementById("joinLocation");
const scCreator = document.getElementById("creator");

const scDeleteBtn = document.getElementById("joinDeleteBtn");

const scJoinBtn = document.getElementById("joinYesBtn");
const scDeclineBtn = document.getElementById("joinNoBtn");

const scJoinCount = document.getElementById("joinYesCount");
const scDeclineCount = document.getElementById("joinNoCount");

const scParticipantStatus = document.getElementById("participantStatus");

let latestScheduleId = null
let myChatRoomMemberId = null;

//--모달 동작 관련--
function openJoinModal() {
  // loadRecentSchedule();
  //TODO
  scModal.style.display = "flex";
}

function closeJoinModal() {
  scModal.style.display = "none";
}

if (scModal) {
  scModal.addEventListener("click", (e) => {
    if (e.target === scModal) {
      closeJoinModal();
    }
  });
}

scCloseBtn.addEventListener("click", () => {
  closeJoinModal();
})

//--최근 일정 로드--

async function loadRecentSchedule() {
  const roomId = document.getElementById("currentRoomId").value;
  if (!roomId) {
    return;
  }
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
    fillScheduleData(data);
    fillRecentScheduleBar(data);
    //scOpenBtn.style.display = "inline-block";

  } catch (err) {
    console.log("일정 없음 : " + err);
    fillRecentScheduleBar(null);
    //scOpenBtn.style.display = "none";
  }
}

window.addEventListener('room:active', (e) => {
  loadRecentSchedule();
})

//--일정 데이터 삽입--

function fillScheduleData(data) {
  latestScheduleId = data.scheduleId;
  myChatRoomMemberId = data.memberId;

  scTitle.textContent = data.title;
  scDesc.textContent = data.detail;
  scLocation.textContent = data.location ?? '-';

  const dateStr = data.startAt.replace("T", " ").slice(0, 16);
  scDate.textContent = `일시: ${dateStr}`;

  scCreator.value = data.scheduleCreatorId;

  scJoinCount.textContent = data.yesCount ?? 0;
  scDeclineCount.textContent = data.noCount ?? 0;
  //삭제버튼 활성/비활성화
  if (data.isCreator) {
    scDeleteBtn.style.display = "block";
  } else {
    scDeleteBtn.style.display = "none";
  }

  fillJoinStatus(data.participantStatus);
}

function fillJoinStatus(status) {
  switch (status) {
    case "JOINED":
      toggleSelect(scJoinBtn);
      break;
    case "DECLINED":
      toggleSelect(scDeclineBtn);
      break;
    case "PENDING":
      toggleSelect(null);
      break;
  }
}

//참가/불참 버튼 라디오 효과 추가
function toggleSelect(selectedBtn) {
  scJoinBtn.classList.remove("active");
  scDeclineBtn.classList.remove("active");
  if (selectedBtn !== null) {
    selectedBtn.classList.add("active");
  }
}

scJoinBtn.addEventListener("click", async (e) => {
  toggleSelect(scJoinBtn);
  await updateParticipant("JOINED");
});
scDeclineBtn.addEventListener("click", async (e) => {
  toggleSelect(scDeclineBtn);
  await updateParticipant("DECLINED");
});

async function updateParticipant(status) {
  if (!latestScheduleId || !myChatRoomMemberId) {
    console.error(
        "latestScheduleId: " + latestScheduleId + " myChatRoomMemberId: "
        + myChatRoomMemberId);
    return;
  }
  const payload = {
    participantStatus: status,
    scheduleId: latestScheduleId,
    memberId: myChatRoomMemberId
  };
  const tokenMeta = document.querySelector('meta[name="_csrf"]');
  const headerMeta = document.querySelector('meta[name="_csrf_header"]');

  const headers = {
    "Content-Type": "application/json"
  };

  if (tokenMeta && headerMeta) {
    headers[headerMeta.content] = tokenMeta.content;
  }

  const response = await fetch("/chat-rooms/joinSelect", {
    method: "PUT",
    headers,
    body: JSON.stringify(payload)
  });

  if (!response.ok) {

    throw new Error("저장 실패");
  }
  console.log("저장 성공");
  //TODO
  const data = await response.json();

  fillJoinStatus(data.participantStatus);
  scJoinCount.textContent = data.yesCount;
  scDeclineCount.textContent = data.noCount;

}

function fillRecentScheduleBar(data) {
  const bar = document.getElementById("recentScheduleBar");
  const text = document.getElementById("recentScheduleText");

  if (!bar || !text) {
    return;
  }

  if (!data) {
    bar.style.display = "none";
    return;
  }

  const formattedTime = formatStartTime(data.startAt);
  text.textContent = `🕒 ${formattedTime} · ${data.title}`;

  bar.style.display = "inline-flex";

  // 클릭 시 모달 열기
  bar.onclick = () => {
    fillScheduleData(data);   // 기존 모달 데이터 작성
    openJoinModal();          // 모달 오픈
  };
}

function formatStartTime(startAt) {
  const start = new Date(startAt);
  const now = new Date();

  const diffMs = start - now;
  const diffMin = Math.floor(diffMs / 60000);
  const diffHour = Math.floor(diffMin / 60);

  if (diffMin < 1) {
    return "곧 시작";
  }
  if (diffMin < 60) {
    return `${diffMin}분 후 시작`;
  }
  if (diffHour < 24) {
    return `${diffHour}시간 후 시작`;
  }

  const isTomorrow = start.getDate() === now.getDate() + 1;
  if (isTomorrow) {
    return `내일 ${start.getHours()}시 시작`;
  }

  return `${start.getMonth() + 1}월 ${start.getDate()}일 ${start.getHours()}시`;
}


