<script setup>
  import ClientFrame from '@/components/layout/client/ClientFrame.vue'
  import MainBannerSlider from '@/components/common/MainBannerSlider.vue'
  import RoundCategory from '@/components/common/RoundCategory.vue'
  import LiveBroadcastCard from '@/components/common/LiveBroadcastCard.vue'
  import CardSlider from '@/components/common/CardSlider.vue'
  import CardTable from '@/components/table/CardTable.vue'
  import ProductCard from '@/components/common/ProductCard.vue'
  import AdBannerPair from '@/components/common/SubBannerSlider.vue'
  import http from '@/libs/HttpRequester'
  import { ref, onMounted, computed } from 'vue'
  import { useRouter } from 'vue-router'
  import axios from 'axios'
  import basicThumbnail from '@/assets/images/thumbnail/basic_thumbnail.png';

  // 변수들
  const router = useRouter()
  const nickname = ref('')
  const isLoggedIn = ref(false)

  // 메인 베너
  const mainBanners = ref([])

  // 라이브 방송
  const featuredBroadcast = ref(null)

  // VOD 리스트
  const vodList = ref([])

  // qna 테이블
  const qnaSampleList = ref([])

  // 템플릿 10개
  const rawProductList = ref([])
  const productList = computed(() => { // 가공된 데이터용
    return (rawProductList.value || []).map(tmpl => {
      const price = tmpl.price || 0
      const discountRate = tmpl.discountRate || 0
      const discountedPrice = Math.floor(price * (1 - discountRate / 100))

      return {
        no: tmpl.no,
        title: tmpl.name,
        imageUrl: tmpl.thumbnailPath || 'https://kr.object.ncloudstorage.com/law-n-road/uploads/defaults/template-thumbnail.png',
        originalPrice: `${price.toLocaleString()}원`,
        discountPercent: `${discountRate}`,
        discountedPrice: `${discountedPrice.toLocaleString()}원`,
      }
    })
  })

  // 서브 베너
  const banners = ref([])

  onMounted(async () => {
    const token = localStorage.getItem('token')
    const nick = localStorage.getItem('nickname')

    isLoggedIn.value = !!token
    if (nick && nick !== 'null') {
      nickname.value = nick
    }

    if (!isLoggedIn.value) {
      logout()  // await 없이 호출 (백그라운드에서 실행)
      // return
    }

    try {
      const res = await http.get('/api/public/main/main-banners')
      mainBanners.value = res.data
    } catch (e) {
      console.error('배너 조회 실패:', e)
    }

    try {
      const res = await http.get('/api/public/broadcast/featured')
      const data = res.data

      if (!data || typeof data !== 'object') {
        console.log('대표 방송이 없습니다.')
        featuredBroadcast.value = null
      } else {
        featuredBroadcast.value = {
          isLive: true,
          videoEmbedUrl: `/client/broadcasts/${data.broadcastNo}?embed=true`,
          thumbnail: data.thumbnailPath || basicThumbnail,
          title: data.title,
          tags: data.keywords || [],
          hostImage: data.lawyerProfile,
          hostName: data.lawyerName,
          hostDesc: data.categoryName,
          broadcastNo: data.broadcastNo
        }
      }
    } catch (e) {
      console.error('대표 방송 정보 조회 실패:', e)
    }

    try {
      const res = await http.get('/api/public/vod/all') // API 경로 변경

      vodList.value = res.data.map(vod => ({
        no: vod.vodNo,
        title: vod.title,
        thumbnail: vod.thumbnailPath || basicThumbnail,
        link: `/vod/${vod.broadcastNo}`
      }))

      console.log('[vodList]', vodList.value)
    } catch (e) {
      console.error('VOD 리스트 불러오기 실패:', e)
    }

    try {
      const res2 = await http.get('/api/public/main/latest')
      qnaSampleList.value = res2.data
      // console.log(qnaSampleList.value)
    } catch (e) {
      console.error('QNA 상담글 조회 실패:', e)
    }

    try {
      const res3 = await http.get('/api/public/main/templates/popular')
      rawProductList .value = res3.data
      // console.log(rawProductList .value)
    } catch (e) {
      console.error('템플릿 top10 조회 실패:', e)
    }

    try {
      const res4 = await http.get('/api/public/main/sub-banners')
      console.log(res4.data)
      banners.value = res4.data;
    } catch (e) {
      console.error('서브 베너 조회 실패:', e)
    }
  })

  // 로그아웃 처리
  const logout = async () => {
    const userNo = localStorage.getItem('no')


    try {
      // ✅ 0. 백엔드에 로그아웃 요청 → RefreshToken 삭제
      if (userNo) {
        await axios.post('/api/auth/logout', { userNo: Number(userNo) })
      }
    } catch (err) {
      console.error('🔴 로그아웃 요청 실패:', err)
    }

    // ✅ 1. 로컬스토리지 항목 삭제
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('accountType')
    localStorage.removeItem('name')
    localStorage.removeItem('nickname')
    localStorage.removeItem('no')

    // ✅ 2. Axios 인증 헤더 제거
    delete axios.defaults.headers.common['Authorization']

    // ✅ 3. 프론트 상태 초기화
    isLoggedIn.value = false
    nickname.value = '회원'

    // ✅ 4. 콘솔 로그 출력: 삭제 여부 확인
    console.log('[로그아웃 완료] localStorage 상태 확인:')
    console.log('token:', localStorage.getItem('token'))
    console.log('refreshToken:', localStorage.getItem('refreshToken'))
    console.log('accountType:', localStorage.getItem('accountType'))
    console.log('name:', localStorage.getItem('name'))
    console.log('nickname:', localStorage.getItem('nickname'))

    // ✅ 5. 로그인 페이지로 이동 + 새로고침
    // router.push('/login')
    // setTimeout(() => location.reload(), 100)
  }

  // 동그라미 카테고리
  const roundCategories = [
    { icon: 'fas fa-car-crash', label: '사고 발생/처리', link: '/search?keyword=사고 발생/처리' },
    { icon: 'fas fa-balance-scale', label: '중대사고·형사처벌', link: '/search?keyword=중대사고·형사처벌' },
    { icon: 'fas fa-beer', label: '음주·무면허 운전', link: '/search?keyword=음주·무면허 운전' },
    { icon: 'fas fa-gavel', label: '보험·행정처분', link: '/search?keyword=보험·행정처분' },
    { icon: 'fas fa-search', label: '과실 분쟁', link: '/search?keyword=과실 분쟁' },
    { icon: 'fas fa-bicycle', label: '차량 외 사고', link: '/search?keyword=차량 외 사고' },
  ]

  // 라이브 방송박스 (대기화면)
  const liveBroadcast = {
    thumbnail: '/img/vod/thumbnails/thumbnail_waiting.png',
    title: '뺑소니 합의금 - 이것만 기억하세요!',
    tags: ['합의', '뺑소니', '음주뺑소니'],
    hostImage: '/img/profiles/kim.png',
    hostName: '김서연 변호사',
    hostDesc: '교통사고 전문',
    link: '/live.html'
  }

  // 방송 참여 함수
  const handleFeaturedClick = async () => {
    try {
      const res = await http.get(`/api/client/broadcast/${featuredBroadcast.value.broadcastNo}/token`)
      const { token, sessionId } = res.data

      // 예: 방송 페이지로 이동하거나 모달로 OpenVidu 연결 시작
      router.push(`/client/broadcasts/${featuredBroadcast.value.broadcastNo}`)
    } catch (e) {
      console.error('방송 참여 실패:', e)
    }
  }

</script>

<template>
  <!-- 의뢰인 타입 본문 콘텐츠 -->
  <ClientFrame>

    <!-- 메인 베너 -->
    <MainBannerSlider
        v-if="mainBanners.length > 0"
        :banners="mainBanners"
        defaultBadgeText="로앤로드 대표 서비스"
    />

    <!-- 동그라미 카테고리 -->
    <RoundCategory :categories="roundCategories" />

    <div class="row">
      <div class="col-12 mb-6">
        <div class="d-flex justify-content-between align-items-center">
          <h3 class="mb-0">변호사와 함께하는 실시간 라이브 방송</h3>
          <h5 class="mb-0 text-muted me-3" style="cursor: pointer;" @click="router.push('/client/broadcasts/list')">더 보러가기 ></h5>
        </div>
      </div>
    </div>

    <LiveBroadcastCard
        v-if="featuredBroadcast"
        :broadcast="featuredBroadcast"
        @click="handleFeaturedClick"
    />

    <div v-else class="text-center py-5">
      <img :src="basicThumbnail" alt="대기 중 썸네일" style="max-width: 800px; width: 100%; border-radius: 12px;">
      <h4 class="mt-4 text-muted">현재 방송 중인 콘텐츠가 없습니다.</h4>
      <p class="text-secondary">곧 새로운 방송이 시작될 예정이에요. 기대해주세요!</p>
    </div>

    <!-- VOD 방송 다시보기 -->
    <div class="row">
      <div class="col-12 mb-6">
        <div class="d-flex justify-content-between align-items-center">
          <h3 class="mb-0">VOD 방송 다시보기</h3>
          <h5 class="mb-0 text-muted me-3" style="cursor: pointer;" @click="router.push('/vod/list')">더 보러가기 ></h5>
        </div>
      </div>
    </div>
    <!-- <CardSlider :replayItems="vodList" :itemsPerRow="4" :rowsPerSlide="1" /> -->
    <CardSlider :replayItems="vodList" :itemsPerRow="3" :rowsPerSlide="2" />

    <!-- QNA 게시판 테이블 -->
    <div class="row">
      <div class="col-12 mb-6">
        <div class="d-flex justify-content-between align-items-center">
          <h3 class="mb-0">나와 비슷한 사례 찾아보기</h3>
          <a href="/qna">
            <h5 class="mb-0 text-muted me-3" style="cursor: pointer;">더 보러가기 ></h5>
          </a>
        </div>
      </div>
    </div>
    <CardTable v-if="qnaSampleList.length > 0" :List="qnaSampleList" :maxLines="4" />

    <div v-else class="text-center text-muted py-5">
      등록된 상담글이 없습니다.
    </div>

    <!-- 서브 베너 -->
    <AdBannerPair :banners="banners" />

    <!-- 템플릿 상품 판매 -->
    <div class="row">
      <div class="col-12 mb-6">
        <div class="d-flex justify-content-between align-items-center">
          <h3 class="mb-0">많이 찾는 법률문서 베스트 10</h3>
          <a href="/templates">
            <h5 class="mb-0 text-muted me-3" style="cursor: pointer;">더 보러가기 ></h5>
          </a>
        </div>
      </div>
    </div>

    <div v-if="productList.length > 0" class="row g-4 row-cols-lg-5 row-cols-2 row-cols-md-3">
      <div class="col-md-3 mb-4" v-for="product in productList" :key="product.no">
        <ProductCard
            :no="product.no"
            :imageUrl="product.imageUrl"
            :title="product.title"
            :originalPrice="product.originalPrice"
            :discountPercent="product.discountPercent"
            :discountedPrice="product.discountedPrice"
        />
      </div>
    </div>

    <div v-else class="text-center text-muted py-5">
      등록된 상품이 없습니다.
    </div>

  </ClientFrame>

</template>

<style scoped>

</style>
