-- KEYS[1] : match:wait:{LEVEL}  (Set)
-- KEYS[2] : match:geo:{LEVEL}   (ZSet as GEO)
-- ARGV[1] : radius meters (e.g., "3000")
-- ARGV[2] : lock ttl ms   (e.g., "5000")

local waitKey = KEYS[1]
local geoKey  = KEYS[2]
local radius  = tonumber(ARGV[1]) or 3000
local lockTtl = tonumber(ARGV[2]) or 5000

local function lockUser(u)
    local lk = "lock:user:" .. u
    local ok = redis.call("SET", lk, "1", "NX", "PX", lockTtl)
    if ok then return true else return false end
end

-- 1) 대기세트에서 앵커 뽑기
local anchor = redis.call("SRANDMEMBER", waitKey)
if not anchor then return {} end

-- 2) 앵커 락
if not lockUser(anchor) then return {} end

-- 3) 앵커 위치 확인 (없으면 정리)
local pos = redis.call("GEOPOS", geoKey, anchor)
if (not pos) or (not pos[1]) then
    redis.call("SREM", waitKey, anchor)
    redis.call("DEL", "lock:user:" .. anchor)
    return {}
end

-- 4) 반경 내 후보 검색 (가까운 순)
local candidates = redis.call(
        "GEORADIUSBYMEMBER", geoKey, anchor, radius, "m", "WITHDIST", "ASC", "COUNT", 50
)

local partner = nil
if candidates and #candidates > 0 then
    for i = 1, #candidates do
        local c = candidates[i][1]
        if c ~= anchor then
            if redis.call("SISMEMBER", waitKey, c) == 1 then
                if lockUser(c) then
                    partner = c
                    break
                end
            end
        end
    end
end

if not partner then
    redis.call("DEL", "lock:user:" .. anchor)
    return {}
end

-- 5) 성공: 대기/지오에서 제거
redis.call("SREM", waitKey, anchor)
redis.call("SREM", waitKey, partner)
redis.call("ZREM", geoKey, anchor)
redis.call("ZREM", geoKey, partner)

return { anchor, partner }


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

-- 3km, run level 적용 전
--local setKey = KEYS[1]
--local ttl = tonumber(ARGV[1])
--
---- 대기열에서 2명 랜덤 추출
--local users = redis.call('SRANDMEMBER', setKey, 2)
--if (users == nil or #users < 2) then
--    return {} -- 대기자가 2명 미만이면 종료
--end
--
---- 락 시도
--for i = 1, #users do
--    local lockKey = 'lock:user:' .. users[i]
--    local ok = redis.call('SETNX', lockKey, '1') -- 락 생성 시도
--    if ok == 1 then
--        redis.call('PEXPIRE', lockKey, ttl)       -- TTL 부여
--    else
--        -- 한 명이라도 락 실패하면 전부 중단
--        -- 이미 잠긴 유저는 다른 워커가 매칭 중
--        return {}
--    end
--end
--
---- 두 유저를 큐에서 제거 (중복 매칭 방지)
--redis.call('SREM', setKey, users[1], users[2])
--
---- 성공적으로 매칭된 두 유저 ID 반환
--return users
