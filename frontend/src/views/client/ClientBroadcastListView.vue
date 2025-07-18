  <script setup>
  import {ref, onMounted, onUnmounted} from 'vue'
  import {useRouter} from 'vue-router'
  import ClientFrame from '@/components/layout/client/ClientFrame.vue'
  import {makeApiRequest} from "@/libs/axios-auth.js"
  import basicThumbnail from '@/assets/images/thumbnail/basic_thumbnail.png';
  import http from '@/libs/HttpRequester.js'

  const router = useRouter()
  const broadcasts = ref([])
  const hoveredCard = ref(null)

  // 무한 스크롤 상태
  const page = ref(1)
  const size = 9
  const totalPages = ref(1)
  const isLoading = ref(false)

  const sort = ref('recent')

  let scrollTimeout = null
  let viewerCountTimer = null

  const fetchLiveBroadcasts = async () => {
    try {
      //await new Promise(resolve => setTimeout(resolve, 800)); // 테스트용 딜레이
      const res = await makeApiRequest({
        method: 'get',
        url: '/api/client/broadcast/live',
        params: {page: page.value, size, sort: sort.value}
      })

      if (res?.data) {
        //broadcasts.value.push(...res.data.content)
        // ① 각 방송 아이템에 viewerCount:0 초기값 추가
        const items = res.data.content.map(b => ({...b, viewerCount: 0}))
        broadcasts.value.push(...items)
        totalPages.value = res.data.totalPages
        page.value += 1
        // ② 방금 추가된 것만 바로 한번 조회
        items.forEach(b => updateOneViewerCount(b))
      }
    } catch (err) {
      console.error('❌ 방송 목록 불러오기 실패:', err)
    } finally {
      isLoading.value = false
    }
  }

  function updateOneViewerCount(broadcast) {
    makeApiRequest({
      method: 'get',
      url: `/api/public/broadcast/${broadcast.broadcastNo}/viewer-count`
    })
        .then(res => {
          broadcast.viewerCount = res.data
        })
        .catch(() => {
          /* 실패해도 무시 */
        })
  }

  // 전체 리스트를 주기적으로 갱신
  function startViewerCountPolling() {
    viewerCountTimer = setInterval(() => {
      broadcasts.value.forEach(b => updateOneViewerCount(b))
    }, 5000) // 5초마다
  }

  const setSort = async (type) => {
    if (sort.value === type) return
    sort.value = type
    page.value = 1
    broadcasts.value = []
    isLoading.value = true
    await fetchLiveBroadcasts()
  }

  const handleScroll = () => {
    if (scrollTimeout) return

    scrollTimeout = setTimeout(() => {
      const scrollY = window.scrollY
      const viewportHeight = window.innerHeight
      const fullHeight = document.documentElement.scrollHeight

      if (!isLoading.value && page.value <= totalPages.value &&
          scrollY + viewportHeight + 200 >= fullHeight) {
        isLoading.value = true
        fetchLiveBroadcasts()
      }

      scrollTimeout = null
    }, 200)
  }

  const goToBroadcast = (broadcastNo) => {
    router.push(`/client/broadcasts/${broadcastNo}`)
  }

  const formatStartTime = (isoString) => {
    const date = new Date(isoString)
    const MM = String(date.getMonth() + 1).padStart(2, '0')
    const DD = String(date.getDate()).padStart(2, '0')
    const hh = String(date.getHours()).padStart(2, '0')
    const mm = String(date.getMinutes()).padStart(2, '0')
    return `${MM}-${DD} ${hh}:${mm} 방송시작`
  }

  onMounted(async () => {
    try {
      await makeApiRequest({
        method: 'get',
        url: '/api/public/broadcast/expire-overdue'
      })
      console.log('⏱ 방송 상태 갱신 완료')
    } catch (err) {
      console.warn('방송 만료 처리 실패:', err)
    }

    isLoading.value = true
    await fetchLiveBroadcasts()
    // 뷰어카운트 폴링 시작
    startViewerCountPolling()
    window.addEventListener('scroll', handleScroll)
  })

  onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll)
    clearInterval(viewerCountTimer)
  })
  </script>

  <template>
    <ClientFrame>
      <div class="container py-4">
        <h2 class="fs-3 fw-bold text-primary mb-4">라이브 방송 목록</h2>

        <!-- 방송이 하나도 없을 때 -->
        <div v-if="broadcasts.length === 0" class="text-center py-5 text-muted fs-5">
          현재 진행중인 방송이 없습니다.
        </div>

        <!-- 방송 목록 -->
        <div v-else class="row g-4">
          <div
              v-for="item in broadcasts"
              :key="item.broadcastNo"
              class="col-12 col-md-6 col-lg-4"
          >
            <div
                class="card h-100 shadow-sm hover-effect"
                @click="goToBroadcast(item.broadcastNo)"
                @mouseenter="hoveredCard = item.broadcastNo"
                @mouseleave="hoveredCard = null"
            >
              <!-- 썸네일 + 뱃지 -->
              <div class="position-relative rounded-top overflow-hidden" style="aspect-ratio: 16 / 9;">
                <img
                    :src="item.thumbnailPath || basicThumbnail"
                    class="w-100 h-100 object-fit-cover"
                    alt="썸네일"
                />

                <!-- LIVE 뱃지 -->
                <div class="position-absolute top-0 start-0 m-2">
                  <span
                      class="text-white fw-bold px-2 py-1 rounded-pill small d-flex align-items-center gap-1 live-badge">
                    <span class="blink">🔴</span> LIVE
                  </span>
                </div>

                <!-- 시청자 수 뱃지 -->
                <div
                    class="position-absolute top-0 end-0 m-2 viewer-count-badge"
                    :class="{ 'visible': hoveredCard === item.broadcastNo }"
                >
                  {{ item.viewerCount }}명 시청 중
                </div>

                <!-- 방송 시작 시간 -->
                <div
                    class="position-absolute bottom-0 start-0 w-100 text-white text-center py-1 small start-time-label"
                    :class="{ 'visible': hoveredCard === item.broadcastNo }"
                >
                  {{ formatStartTime(item.startTime) }}
                </div>
              </div>

              <!-- 카드 본문 -->
              <div class="card-body d-flex gap-3">
                <img
                    :src="item.profile || '/images/default-profile.png'"
                    alt="프로필"
                    class="rounded-circle border"
                    style="width: 56px; height: 56px; object-fit: cover;"
                />
                <div class="flex-grow-1">
                  <h5 class="mb-1 fs-5 fw-bold text-primary text-wrap">
                    {{ item.title }}
                  </h5>
                  <p class="mb-2 text-dark fw-semibold" style="font-size: 0.95rem;">
                    {{ item.lawyerName }} 변호사
                  </p>
                  <div class="d-flex flex-wrap gap-1">
                    <span
                        v-for="(kw, i) in item.keywords"
                        :key="i"
                        class="badge bg-primary bg-opacity-10 text-primary fw-semibold"
                        style="font-size: 0.75rem;"
                    >
                      # {{ kw }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 로딩 중 메시지 -->
          <div v-if="isLoading" class="col-12 text-center my-4 text-muted">
            불러오는 중...
          </div>
        </div>
      </div>
    </ClientFrame>
  </template>


  <style scoped>
  .card:hover {
    box-shadow: 0 0 0.5rem rgba(0, 0, 0, 0.15);
    transform: scale(1.01);
    transition: all 0.2s;
  }

  .hover-effect {
    cursor: pointer;
  }

  .object-fit-cover {
    object-fit: cover;
  }

  @keyframes blink {
    0%, 100% {
      opacity: 1;
    }
    50% {
      opacity: 0.2;
    }
  }

  .blink {
    animation: blink 1s infinite;
  }

  .live-badge {
    box-shadow: 0 0 5px rgba(255, 0, 0, 0.6);
    background-color: rgba(220, 53, 69, 0.85);
    backdrop-filter: blur(2px);
  }

  /* 방송 시작 시간 라벨 */
  .start-time-label {
    opacity: 0;
    background-color: rgba(0, 0, 0, 0.5);
    transition: opacity 0.3s ease;
  }

  .start-time-label.visible {
    opacity: 1;
  }

  /* 시청자 수 뱃지 (hover 시 표시) */
  .viewer-count-badge {
    opacity: 0;
    transition: opacity 0.3s ease;
    background-color: rgba(0, 0, 0, 0.5);
    color: white;
    padding: 4px 10px;
    font-size: 0.75rem;
    border-radius: 1rem;
    font-weight: bold;
    backdrop-filter: blur(2px);
  }

  .viewer-count-badge.visible {
    opacity: 1;
  }
  </style>
