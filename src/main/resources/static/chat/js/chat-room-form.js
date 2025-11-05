// 현재 페이지가 생성모드인지 수정모드인지 판별
document.addEventListener("DOMContentLoaded", async () => {
  const pathParts = window.location.pathname.split("/");
  const chatRoomId = pathParts[pathParts.length - 1]; // 마지막 부분이 roomId
  const mode = pathParts.includes("edit") ? "edit" : "create";

  // 이미지 미리보기
  const imageInput = document.getElementById('imageInput');
  const preview = document.getElementById('preview');

  imageInput.addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (!file) return;

    if (file) {
      const reader = new FileReader();
      reader.onload = function(e) {
        preview.src = e.target.result;
      }
      reader.readAsDataURL(file);
    }
  });

  // 채팅방 폼 제출
  document.getElementById("chatRoomForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    const form = document.getElementById("chatRoomForm");
    const fd = new FormData(form); // _csrf 포함 + 모든 입력 포함

    // 만약 JS로 값 덮어써야 한다면 append 말고 set 사용
    fd.set("chatRoomName", document.getElementById("chatRoomName").value);
    fd.set("description", document.getElementById("chatRoomDesc").value);
    fd.set("region", document.getElementById("chatRoomRegion").value);
    fd.set("chatRoomType", "GROUP");

    const file = document.getElementById("imageInput").files?.[0];
    if (file) fd.set("chatRoomImage", file); // 이미 포함돼 있으면 굳이 필요 없음

    let url = "/api/chats";
    let method = "POST";

    if (mode === "edit" && chatRoomId) {
      url = `/api/chats/${chatRoomId}`;
      method = "PUT";
    }

    const res = await fetch(url, { method, body: fd });

    if (res.ok) {
      alert(mode === "create" ? "채팅방이 생성되었습니다!" : "채팅방이 수정되었습니다!");
      window.location.href = "/chat-room/list";
    } else {
      const text = await res.text().catch(() => "");
      alert(`오류가 발생했습니다. 다시 시도해주세요.\n${text}`);
    }
  });
});
