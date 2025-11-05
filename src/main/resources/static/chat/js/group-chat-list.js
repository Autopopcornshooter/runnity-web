async function joinAndGo(chatRoomId) {
  try {
    const token  = document.querySelector('meta[name="_csrf"]').content;
    const header = document.querySelector('meta[name="_csrf_header"]').content;

    const res = await fetch(`/api/chats/${chatRoomId}/join`, {
      method: 'POST',
      headers: {[header]: token}
    });

    if (res.status === 201 || res.ok) {
      // 원하는 최종 경로로 이동
      window.location.href = `/chat-room/my-chat-list/${chatRoomId}`;
      return;
    }

    if (res.status === 401) {
      alert('로그인이 필요합니다');
      location.href = '/api/auth/signIn';
      return;
    }
    if (res.status === 403) {
      alert('권한이 없습니다');
      return;
    }

    const text = await res.text();
    alert(`JOIN 실패: ${text}`);
  } catch (e) {
    console.error(e);
    alert('네트워크 오류가 발생했어요.');
  }
}