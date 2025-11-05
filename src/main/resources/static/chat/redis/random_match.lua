--[[
duo_match.lua
-----------------------------------------------
대기열에서 2명을 원자적으로 꺼내서 락(lock:user:{id})을 잡고,
매칭 성공 시 두 유저의 ID를 반환한다.

KEYS[1] : 매칭 대기열 key (예: match:queue:default)
ARGV[1] : 락 TTL (밀리초 단위, 예: 5000)
-----------------------------------------------
]]

-- KEYS 및 ARGV
local setKey = KEYS[1]
local ttl = tonumber(ARGV[1])

-- 대기열에서 2명 랜덤 추출
local users = redis.call('SRANDMEMBER', setKey, 2)
if (users == nil or #users < 2) then
    return {} -- 대기자가 2명 미만이면 종료
end

-- 락 시도
for i = 1, #users do
    local lockKey = 'lock:user:' .. users[i]
    local ok = redis.call('SETNX', lockKey, '1') -- 락 생성 시도
    if ok == 1 then
        redis.call('PEXPIRE', lockKey, ttl)       -- TTL 부여
    else
        -- 한 명이라도 락 실패하면 전부 중단
        -- 이미 잠긴 유저는 다른 워커가 매칭 중
        return {}
    end
end

-- 두 유저를 큐에서 제거 (중복 매칭 방지)
redis.call('SREM', setKey, users[1], users[2])

-- 성공적으로 매칭된 두 유저 ID 반환
return users
