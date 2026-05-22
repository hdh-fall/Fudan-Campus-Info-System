<template>
  <div class="home">
    <el-card class="welcome-card">
      <h2>欢迎使用复旦校园百事通</h2>
      <p>本系统提供校园信息查询服务，包括校区建筑、设施、课程、教师和活动等信息。</p>
    </el-card>

    <!-- 数据统计卡片 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon size="40" color="#409EFF"><OfficeBuilding /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.campusCount }}</div>
              <div class="stat-label">校区</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon size="40" color="#67C23A"><MapLocation /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.buildingCount }}</div>
              <div class="stat-label">建筑</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon size="40" color="#E6A23C"><CoffeeCup /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.facilityCount }}</div>
              <div class="stat-label">设施</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon size="40" color="#F56C6C"><Calendar /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.eventCount }}</div>
              <div class="stat-label">近期活动</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card class="feature-card" @click="handleNavigate('campus')">
          <el-icon size="50"><OfficeBuilding /></el-icon>
          <h3>校区建筑</h3>
          <p>查询各校区建筑信息</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="feature-card" @click="handleNavigate('facility')">
          <el-icon size="50"><CoffeeCup /></el-icon>
          <h3>校园设施</h3>
          <p>查找食堂、图书馆等设施</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="feature-card" @click="handleNavigate('course')">
          <el-icon size="50"><Reading /></el-icon>
          <h3>课程教师</h3>
          <p>浏览课程和教师信息</p>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card class="feature-card" @click="handleNavigate('event')">
          <el-icon size="50"><Calendar /></el-icon>
          <h3>校园活动</h3>
          <p>查看近期校园活动</p>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="feature-card" @click="handleNavigate('user')">
          <el-icon size="50"><User /></el-icon>
          <h3>个人中心</h3>
          <p>查看个人信息和查询历史</p>
        </el-card>
      </el-col>
    </el-row>

    <!-- 管理员入口 -->
    <el-row v-if="isAdmin" :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card class="feature-card admin-card" @click="handleNavigate('admin')">
          <el-icon size="50"><Setting /></el-icon>
          <h3>管理后台</h3>
          <p>管理系统数据和用户信息</p>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>🤖 智能问答</span>
          <el-text size="small" type="success">支持自然语言提问</el-text>
        </div>
      </template>
      
      <el-alert
        title="试试这样问："
        type="info"
        :closable="false"
        style="margin-bottom: 15px;"
      >
        <template #default>
          <div style="font-size: 13px; line-height: 1.8;">
            • “复旦有哪些食堂？”<br/>
            • “邯郸校区有哪些教学楼？”<br/>
            • “数据库课程是谁开的？”<br/>
            • “最近有哪些校园讲座？”
          </div>
        </template>
      </el-alert>
      
      <el-input
        v-model="nlQuestion"
        placeholder="请输入您的问题，例如：复旦有哪些食堂？"
        size="large"
        clearable
        @keyup.enter="handleNLQuery"
      >
        <template #prefix>
          <el-icon><ChatDotRound /></el-icon>
        </template>
        <template #append>
          <el-button type="success" @click="handleNLQuery" :loading="nlLoading">
            <el-icon><Promotion /></el-icon>
            提问
          </el-button>
        </template>
      </el-input>
      
      <!-- 问答结果展示 -->
      <div v-if="nlResult" style="margin-top: 20px;">
        <el-divider content-position="left">
          <el-tag type="success">{{ formatQueryType(nlResult.queryType) }}</el-tag>
        </el-divider>
        
        <el-empty v-if="nlResult.count === 0" description="没有找到相关信息" :image-size="100" />
        
        <div v-else>
          <el-alert
            :title="`找到 ${nlResult.count} 条相关结果`"
            type="success"
            :closable="false"
            style="margin-bottom: 15px;"
          />
          
          <el-table :data="nlResult.data" stripe max-height="400">
            <el-table-column
              v-for="(value, key) in nlResult.data[0]"
              :key="key"
              :prop="key"
              :label="formatLabel(key)"
              min-width="120"
            >
              <template #default="scope">
                {{ formatCellValue(key, scope.row[key]) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>🔍 快速搜索</span>
          <el-text size="small" type="info">支持建筑、设施、课程、教师、活动</el-text>
        </div>
      </template>
      <el-input
        v-model="searchKeyword"
        placeholder="请输入关键词，例如：光华楼、食堂、数据库..."
        size="large"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
      
      <!-- 搜索结果 -->
      <div v-if="searchResults.length > 0" style="margin-top: 20px;">
        <h4>搜索结果</h4>
        <el-table :data="searchResults" style="width: 100%" max-height="400">
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="type" label="类型" width="120" />
          <el-table-column prop="category" label="类别" width="120" />
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button size="small" @click="viewDetail(scope.row)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { OfficeBuilding, MapLocation, CoffeeCup, Reading, Calendar, User, Search, ChatDotRound, Promotion, Setting } from '@element-plus/icons-vue'
import { campusAPI, buildingAPI, facilityAPI, eventAPI, courseAPI, teacherAPI } from '../api'
import { ElMessage } from 'element-plus'

export default {
  name: 'HomeView',
  components: {
    OfficeBuilding,
    MapLocation,
    CoffeeCup,
    Reading,
    Calendar,
    User,
    Search,
    ChatDotRound,
    Promotion,
    Setting
  },
  emits: ['navigate'],
  setup(props, { emit }) {
    const searchKeyword = ref('')
    const searchResults = ref([])
    
    // 自然语言查询相关
    const nlQuestion = ref('')
    const nlLoading = ref(false)
    const nlResult = ref(null)
    
    // 检查是否为管理员
    const isAdmin = computed(() => {
      const currentUserStr = localStorage.getItem('currentUser')
      if (currentUserStr) {
        try {
          const currentUser = JSON.parse(currentUserStr)
          return currentUser && currentUser.role === 'admin'
        } catch (e) {
          console.error('解析用户信息失败:', e)
        }
      }
      return false
    })
    
    const stats = ref({
      campusCount: 0,
      buildingCount: 0,
      facilityCount: 0,
      eventCount: 0
    })

    // 加载统计数据
    const loadStats = async () => {
      try {
        const campuses = await campusAPI.getAllCampuses()
        stats.value.campusCount = campuses.length
        
        // 获取建筑总数（通过搜索所有）
        const buildings = await buildingAPI.searchBuildings('')
        stats.value.buildingCount = buildings.length
        
        // 获取设施总数
        const facilities = await facilityAPI.searchFacilities('')
        stats.value.facilityCount = facilities.length
        
        // 获取近期活动数（未来15天）
        const events = await eventAPI.getUpcomingEvents(15)
        stats.value.eventCount = events.length
      } catch (error) {
        console.error('加载统计数据失败:', error)
      }
    }

    const handleNavigate = (page) => {
      emit('navigate', page)
    }

    const handleSearch = async () => {
      if (!searchKeyword.value.trim()) {
        searchResults.value = []
        return
      }
      
      try {
        const results = []
        
        // 搜索建筑
        const buildings = await buildingAPI.searchBuildings(searchKeyword.value)
        buildings.forEach(b => {
          results.push({
            id: b.buildingId,
            name: b.name,
            type: b.type,
            category: '建筑',
            data: b
          })
        })
        
        // 搜索设施
        const facilities = await facilityAPI.searchFacilities(searchKeyword.value)
        facilities.forEach(f => {
          results.push({
            id: f.facilityId,
            name: f.name,
            type: f.type,
            category: '设施',
            data: f
          })
        })
        
        // 搜索课程
        const courses = await courseAPI.searchCourses(searchKeyword.value)
        courses.forEach(c => {
          results.push({
            id: c.courseId,
            name: c.name,
            type: '课程',
            category: '课程',
            data: c
          })
        })
        
        // 搜索教师
        const teachers = await teacherAPI.searchTeachers(searchKeyword.value)
        teachers.forEach(t => {
          results.push({
            id: t.teacherId,
            name: t.name,
            type: t.title || '教师',
            category: '教师',
            data: t
          })
        })
        
        // 搜索活动
        const events = await eventAPI.searchEvents(searchKeyword.value)
        events.forEach(e => {
          results.push({
            id: e.eventId,
            name: e.name,
            type: e.category || '活动',
            category: '活动',
            data: e
          })
        })
        
        searchResults.value = results
        
        if (results.length === 0) {
          ElMessage.info('未找到相关结果')
        }
      } catch (error) {
        ElMessage.error('搜索失败')
        console.error(error)
      }
    }
    
    // 自然语言查询处理
    const handleNLQuery = async () => {
      if (!nlQuestion.value.trim()) {
        ElMessage.warning('请输入您的问题')
        return
      }
      
      // 从 localStorage 获取当前用户ID
      const currentUserStr = localStorage.getItem('currentUser')
      let userId = 1 // 默认用户ID
      if (currentUserStr) {
        try {
          const currentUser = JSON.parse(currentUserStr)
          userId = currentUser.userId || 1
        } catch (e) {
          console.error('解析用户信息失败:', e)
        }
      }
      
      nlLoading.value = true
      try {
        console.log('发起自然语言查询:', nlQuestion.value, '用户ID:', userId)
        const response = await fetch('http://localhost:8080/api/nl-query', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            question: nlQuestion.value,
            userId: userId
          })
        })
        
        if (!response.ok) {
          throw new Error('查询失败')
        }
        
        const result = await response.json()
        console.log('查询结果:', result)
        nlResult.value = result
        
        if (result.count === 0) {
          ElMessage.info('没有找到相关信息')
        }
      } catch (error) {
        console.error('自然语言查询失败:', error)
        ElMessage.error('查询失败，请稍后重试')
      } finally {
        nlLoading.value = false
      }
    }
    
    // 格式化表格列标签
    const formatLabel = (key) => {
      const labelMap = {
        // ===== 基础字段 =====
        name: '名称',
        type: '类型',
        location: '位置',
        building: '所属建筑',
        openTime: '开放时间',
        capacity: '容量',
        description: '描述',
        campus: '校区',
        address: '地址',
        phone: '电话',
        department: '院系',
        credits: '学分',
        semester: '学期',
        time: '时间',
        organizer: '主办方',
        category: '类别',
        title: '职称',
        email: '邮箱',
        eventTime: '活动时间',
        locationDesc: '位置描述',
        floors: '楼层数',
        open_time: '开放时间',
        close_time: '关闭时间',
        contact: '联系方式',
        location_desc: '位置描述',
        semester_offered: '开设学期',
        teachers: '授课教师',
        role: '角色',
        course: '课程',
        
        // ===== AI SQL返回的字段（带别名）===== 
        building_name: '建筑名称',
        campus_name: '校区名称',
        teacher_name: '教师姓名',
        event_name: '活动名称',
        department_name: '院系名称',
        course_name: '课程名称',
        
        // ===== campus表字段 =====
        campus_id: '校区ID',
        contact_phone: '联系电话',
        latitude: '纬度',
        longitude: '经度',
        
        // ===== building表字段 =====
        building_id: '建筑ID',
        
        // ===== facility表字段 =====
        facility_id: '设施ID',
        
        // ===== department表字段 =====
        department_id: '院系ID',
        
        // ===== teacher表字段 =====
        teacher_id: '教师ID',
        
        // ===== course表字段 =====
        course_id: '课程ID',
        
        // ===== event表字段 =====
        event_id: '活动ID',
        event_time: '活动时间',
        
        // ===== user表字段 =====
        user_id: '用户ID',
        username: '用户名',
        grade: '年级',
        created_at: '创建时间',
        
        // ===== course_teacher表字段 =====
        remarks: '备注',
        
        // ===== query_record表字段 =====
        record_id: '记录ID',
        question: '问题',
        query_time: '查询时间',
        result_summary: '结果摘要',
        used_nl2sql: '使用AI',
        client_ip: '客户端IP',
        
        // ===== 通用字段（column_1, column_2等）=====
        column_1: '字段1',
        column_2: '字段2',
        column_3: '字段3',
        column_4: '字段4',
        column_5: '字段5'
      }
      return labelMap[key] || key
    }
    
    // 格式化查询类型
    const formatQueryType = (queryType) => {
      const typeMap = {
        'FACILITY_CANTEEN': '食堂查询',
        'FACILITY_CAFE': '咖啡厅查询',
        'FACILITY_LIBRARY': '图书馆查询',
        'FACILITY_SPORTS': '体育设施查询',
        'FACILITY_LAB': '实验室查询',
        'BUILDING': '建筑查询',
        'CAMPUS': '校区查询',
        'COURSE': '课程查询',
        'TEACHER': '教师查询',
        'EVENT': '活动查询',
        'GENERAL_SEARCH': '搜索'
      }
      return typeMap[queryType] || queryType || '查询'
    }
    
    // 格式化单元格值
    const formatCellValue = (key, value) => {
      if (!value) return '未设置'
      
      // 特殊处理某些字段
      if (key === 'role') {
        return value === 'admin' ? '管理员' : (value || '主讲')
      }
      
      // 处理布尔值
      if (key === 'used_nl2sql') {
        return value ? '是' : '否'
      }
      
      // 处理时间格式（ISO 8601 -> 友好格式）
      if (key === 'time' || key === 'eventTime' || key === 'event_time' || key === 'query_time' || key === 'created_at') {
        try {
          const date = new Date(value)
          return date.toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
          })
        } catch (e) {
          return value
        }
      }
      
      if (key === 'type') {
        const typeMap = {
          '教学楼': '教学楼',
          '图书馆': '图书馆',
          '食堂': '食堂',
          '实验楼': '实验楼',
          '咖啡厅': '咖啡厅',
          '自习室': '自习室',
          '机房': '机房',
          '研讨室': '研讨室',
          '报告厅': '报告厅',
          '实验室': '实验室',
          'canteen': '食堂',
          'cafe': '咖啡厅',
          'library': '图书馆',
          'study_room': '自习室',
          'computer_lab': '机房',
          'seminar_room': '研讨室',
          'lecture_hall': '报告厅',
          'lab': '实验室'
        }
        return typeMap[value] || value
      }
      
      if (key === 'category') {
        const categoryMap = {
          '讲座': '讲座',
          '论坛': '论坛',
          '社团': '社团',
          '展览': '展览',
          '活动': '活动',
          'lecture': '讲座',
          'forum': '论坛',
          'club': '社团',
          'exhibition': '展览',
          'event': '活动'
        }
        return categoryMap[value] || value
      }
      
      return value
    }

    const viewDetail = (item) => {
      // 根据类别导航到对应页面并查看详情
      const pageMap = {
        '建筑': 'campus',
        '设施': 'facility',
        '课程': 'course',
        '教师': 'course',
        '活动': 'event'
      }
      
      const targetPage = pageMap[item.category]
      if (targetPage) {
        // 设置一个全局变量来传递详情信息
        localStorage.setItem('viewDetailData', JSON.stringify({
          category: item.category,
          data: item.data
        }))
        emit('navigate', targetPage)
        ElMessage.success(`正在查看${item.category}详情`)
      } else {
        ElMessage.warning('暂不支持该类型的详情查看')
      }
    }

    onMounted(() => {
      loadStats()
    })

    return {
      searchKeyword,
      searchResults,
      nlQuestion,
      nlLoading,
      nlResult,
      isAdmin,
      stats,
      handleNavigate,
      handleSearch,
      handleNLQuery,
      formatLabel,
      formatQueryType,
      formatCellValue,
      viewDetail
    }
  }
}
</script>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-card {
  text-align: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.welcome-card h2 {
  margin: 0 0 10px 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.feature-card {
  text-align: center;
  cursor: pointer;
  transition: transform 0.3s;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.admin-card {
  background: linear-gradient(135deg, rgba(0, 0, 0, 0.47) 0%, rgba(236, 229, 229, 0) 100%);
  color: rgb(245, 245, 245);
}

.admin-card h3 {
  color: white !important;
}

.feature-card h3 {
  margin: 15px 0 10px 0;
  color: #409EFF;
}

.feature-card p {
  color: #666;
  margin: 0;
}

h4 {
  margin-top: 0;
  color: #303133;
}
</style>
