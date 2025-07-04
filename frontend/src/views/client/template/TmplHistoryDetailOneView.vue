<!-- src/views/client/template/TmplHistoryDetailOneView.vue -->
<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ClientFrame from '@/components/layout/client/ClientFrame.vue'
import http from '@/libs/HttpRequester'
import ChatBox from "@/components/template/ChatBox.vue";

const route  = useRoute()
const router = useRouter()

// URL: /client/template/orders/:tmplNo?orderNo=123&type=EDITOR
const tmplNo       = Number(route.params.tmplNo)
const orderNo      = Number(route.query.orderNo)
const templateType = route.query.type || 'FILE'

// 조회 전용 탭 상태
const selectedTab = ref(templateType === 'EDITOR' ? 'ai' : 'upload')

const detail    = ref(null)
const isLoading = ref(true)

const name         = ref('')
const price        = ref(0)
const discountRate = ref(0)
const categoryNo   = ref(null)
const description  = ref('')
const thumbnailUrl = ref('')
const createdAt    = ref('')
const isDownloaded = ref(false)

// AI 전용
const editorContent   = ref('')
const editorVariables = ref([])

// FILE 전용
const uploadedFiles = ref([])

onMounted(async () => {
  try {
    const { data } = await http.get(
        `/api/client/templates/orders/${tmplNo}`,
        { type: templateType }
    )

    const res = await http.post('/api/client/templates/orders/isDownloaded', {
      orderNo: orderNo,
      tmplNo: tmplNo
    });
    isDownloaded.value = res.data.isDownloaded

    detail.value = data

    console.log(data)

    // 공통
    name.value         = data.name
    price.value        = data.price
    discountRate.value = data.discountRate
    categoryNo.value   = data.categoryNo
    description.value  = data.description
    createdAt.value    = data.createdAt
    thumbnailUrl.value = data.thumbnailPath

    // 타입별
    if (templateType === 'EDITOR') {
      selectedTab.value   = 'ai'
      editorContent.value = data.content
      editorVariables.value = JSON.parse(data.varJson || '[]')
    } else {
      selectedTab.value   = 'upload'
      uploadedFiles.value = JSON.parse(data.pathJson || '[]')
    }
  } catch (e) {
    console.error(e)
    alert('템플릿 정보를 불러오지 못했습니다.')
    router.back()
  } finally {
    isLoading.value = false
  }
})

function goBack() {
  router.back()
}

const categoryNameToNo = {
  '사고 발생/처리': 1,
  '중대사고·형사처벌': 2,
  '음주·무면허 운전': 3,
  '보험·행정처분': 4,
  '과실 분쟁': 5,
  '차량 외 사고': 6
}
const categoryNoToName = Object.fromEntries(
    Object.entries(categoryNameToNo).map(([n, no]) => [no, n])
)

const categoryDisplay = computed(() =>
    categoryNoToName[detail.value?.categoryNo] || '알 수 없음'
)

// 다운로드 함수
const handleDownload = async (file) => {
  try {
    // 1. 다운로드 기록 API 호출 (서버에 다운로드 표시)
    await http.post('/api/client/templates/orders/download', {
      orderNo: orderNo,
      tmplNo: tmplNo,
    })

    // 2. 다운로드 강제 트리거
    const link = document.createElement('a')
    link.href = file.savedPath
    link.download = file.originalName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (err) {
    alert('다운로드 중 오류 발생')
    console.error(err)
  }
}

function insertExample() {
  window.location.href = `http://localhost:5173/client/reservations/${detail.value.lawyerNo}/${detail.value.lawyerName}`
}
</script>

<template>
  <ClientFrame>
    <div class="container py-5">

      <!-- 헤더: 제목 + 뒤로, 액션 버튼 -->
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h3 class="fw-bold mb-0">템플릿 상세 조회</h3>
        <div>
          <button class="btn btn-outline-secondary me-2" @click="goBack">
            ← 뒤로
          </button>
        </div>
      </div>

      <!-- 로딩 / 에러 / 본문 -->
      <div v-if="isLoading" class="text-center">로딩 중…</div>
      <div v-else-if="!detail" class="text-center text-danger">
        주문 내역을 불러올 수 없습니다.
      </div>
      <div v-else class="card p-4">
        <div class="row mb-4">
          <!-- 썸네일 -->
          <div class="col-md-4">
            <label class="form-label">썸네일</label>
            <div
                class="preview-box mb-2 d-flex align-items-center justify-content-center border rounded"
            >
              <img
                  v-if="detail.thumbnailPath"
                  :src="detail.thumbnailPath"
                  alt="썸네일"
                  class="img-fluid h-100"
                  style="object-fit: contain"
              />
              <p v-else class="text-muted text-center">이미지 없음</p>
            </div>
          </div>
          <!-- 메타 정보 -->
          <div class="col-md-8">
            <label class="form-label">템플릿명</label>
            <input v-model="detail.name" disabled class="form-control mb-2" />

            <label class="form-label">카테고리</label>
            <input :value="categoryDisplay" disabled class="form-control mb-2" />

            <label class="form-label">구매가 (원)</label>
            <input
                v-model="detail.price"
                disabled
                type="number"
                class="form-control mb-2"
            />

            <label class="form-label">다운로드 여부</label>
            <input
                :value="isDownloaded ? '다운로드 완료' : '미다운로드'"
                disabled
                class="form-control mb-2"
            />
          </div>
        </div>

        <!-- 변호사 정보 -->

        <!-- 설명이 필요하다면 넣을 공간 -->
        <div v-if="detail.description" class="mb-3">
          <label class="form-label">상세 설명</label>
          <textarea
              v-model="detail.description"
              disabled
              rows="4"
              class="form-control"
          ></textarea>
        </div>

        <!-- 에디터 타입일 때 -->
        <ChatBox
            v-if="templateType === 'EDITOR' && orderNo && tmplNo"
            :description="detail.description"
            :content="detail.content"
           :variables="JSON.parse(detail.varJson)"
            :orderNo="orderNo"
            :tmplNo="tmplNo"
        />

        <!-- 파일 타입일 때 -->
        <div v-else>
          <h5 class="mb-3">첨부 파일</h5>
          <ul class="list-group">
            <li
                v-for="(file, i) in uploadedFiles"
                :key="i"
                class="list-group-item d-flex justify-content-between align-items-center"
            >
              <a href="#" @click.prevent="handleDownload(file)">
                {{ file.originalName }}
              </a>
            </li>
          </ul>
          <p v-if="!uploadedFiles.length" class="text-muted mt-2">
            첨부된 파일이 없습니다.
          </p>
        </div>

        <!-- 설명이 필요하다면 넣을 공간 -->
        <!-- 가운데 정렬된 경고 + 변호사 정보 + 상담 버튼 -->
        <div class="card p-3 mt-5 bg-light-subtle ">

          <!-- 경고 문구: 가운데 정렬 -->
          <div class="text-center">
            <strong class="text-primary">
              템플릿을 사용해 작성된 법률 문서는 자체적으로 법적 효력을 가지지 않습니다. <br>
              작성된 문서를 바탕으로 변호사와의 상담을 받아보세요.
            </strong>
          </div>

          <hr>

          <!-- 변호사 정보 좌측 + 버튼 우측 -->
          <div class="d-flex justify-content-between align-items-center flex-wrap">
            <!-- 변호사 정보 (왼쪽) -->
            <div class="d-flex align-items-center">
              <img
                  :src="detail.lawyerProfileImg"
                  alt="프로필"
                  class="rounded-circle me-3"
                  width="64"
                  height="64"
                  style="object-fit: cover"
              />
              <div>
                <div class="fw-bold">{{ detail.lawyerName }} 변호사 <span class="text-muted">| {{ detail.lawyerOfficeName }}</span></div>
                <div class="text-muted small">{{ detail.lawyerShortIntro }}</div>
                <div class="text-muted small">사무실 전화: {{ detail.lawyerOfficeTel }}</div>
                <div class="text-muted small">주소: {{ detail.fullAddress }}</div>
              </div>
            </div>

            <!-- 상담 버튼 (오른쪽) -->
            <div class="mt-3 mt-md-0">
              <button class="btn btn-outline-primary btn-sm" @click="insertExample">
                변호사 상담 받기
              </button>
            </div>
          </div>

        </div>

      </div>
    </div>
  </ClientFrame>
</template>

<style scoped>
.preview-box {
  width: 100%;
  height: 200px;
  background-color: #f7f8fb;
  border: 1px dashed #ccc;
  overflow: hidden;
}
.form-label {
  font-weight: 500;
}
.preview-box {
  width: 100%;
  height: 285px;
  background-color: #f8f9fa;
  border: 1px dashed #ccc;
  overflow: hidden;
}
.template-wrapper {
  background-color: #f7f8fb;
  border: 1px solid #d6dbe8;
  border-radius: 0.5rem;
}
</style>
