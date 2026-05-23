<template>
  <div class="home">
    <!-- 欢迎卡片 - 带校徽和图片轮播 -->
    <el-card class="welcome-card" :body-style="{ padding: '0px' }">
      <div class="carousel-container">
        <div 
          v-for="(bg, index) in backgrounds" 
          :key="index"
          class="carousel-bg"
          :class="{ active: currentBgIndex === index }"
          :style="{ backgroundImage: `url(${bg})` }"
        />
        <div class="carousel-overlay"></div>
      </div>
      
      <div class="welcome-content">
        <div class="title-section">
          <h2>欢迎使用复旦校园百事通</h2>
          <p>探索美丽校园，发现精彩生活 </p>
        </div>
      </div>
    </el-card>

    <!-- 个性化推荐 -->
    <el-card class="recommend-card">
      <template #header>
        <div class="card-header">
          <span>✨ 为您推荐</span>
          <el-text size="small" type="warning">基于您的查询历史</el-text>
        </div>
      </template>
      
      <el-empty v-if="recommendations.length === 0" description="暂无推荐内容" :image-size="80" />
      
      <div v-else class="recommend-list">
        <div 
          v-for="(item, index) in recommendations" 
          :key="index"
          class="recommend-item"
          @click="handleRecommendClick(item)"
        >
          <div class="recommend-icon">
            <el-icon :size="32" :color="getCategoryColor(item.category)">
              <component :is="getCategoryIcon(item.category)" />
            </el-icon>
          </div>
          <div class="recommend-content">
            <h4>{{ item.title }}</h4>
            <p>{{ item.description }}</p>
            <el-tag size="small" :type="getCategoryTagType(item.category)">{{ getCategoryName(item.category) }}</el-tag>
          </div>
          <div class="recommend-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 数据统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon size="36" color="#409EFF"><OfficeBuilding /></el-icon>
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
            <el-icon size="36" color="#67C23A"><MapLocation /></el-icon>
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
            <el-icon size="36" color="#E6A23C"><CoffeeCup /></el-icon>
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
            <el-icon size="36" color="#F56C6C"><Calendar /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.eventCount }}</div>
              <div class="stat-label">近期活动</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="feature-card" @click="handleNavigate('campus')">
          <el-icon size="48"><OfficeBuilding /></el-icon>
          <h3>校区建筑</h3>
          <p>查询各校区建筑信息</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="feature-card" @click="handleNavigate('facility')">
          <el-icon size="48"><CoffeeCup /></el-icon>
          <h3>校园设施</h3>
          <p>查找食堂、图书馆等设施</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="feature-card" @click="handleNavigate('course')">
          <el-icon size="48"><Reading /></el-icon>
          <h3>课程教师</h3>
          <p>浏览课程和教师信息</p>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="feature-card" @click="handleNavigate('event')">
          <el-icon size="48"><Calendar /></el-icon>
          <h3>校园活动</h3>
          <p>查看近期校园活动</p>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="feature-card" @click="handleNavigate('user')">
          <el-icon size="48"><User /></el-icon>
          <h3>个人中心</h3>
          <p>查看个人信息和查询历史</p>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
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
      
      <div style="display: flex; gap: 12px;">
        <el-input
          v-model="nlQuestion"
          placeholder="请输入您的问题，例如：复旦有哪些食堂？"
          size="large"
          clearable
          @keyup.enter="handleNLQuery"
          style="flex: 1;"
        >
          <template #prefix>
            <el-icon><ChatDotRound /></el-icon>
          </template>
        </el-input>
        <el-button type="success" @click="handleNLQuery" :loading="nlLoading" size="large">
          <el-icon><Promotion /></el-icon>
          提问
        </el-button>
      </div>
      
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

    <el-card>
      <template #header>
        <div class="card-header">
          <span>🔍 快速搜索</span>
          <el-text size="small" type="info">支持建筑、设施、课程、教师、活动</el-text>
        </div>
      </template>
      <div style="display: flex; gap: 12px;">
        <el-input
          v-model="searchKeyword"
          placeholder="请输入关键词，例如：光华楼、食堂、数据库..."
          size="large"
          clearable
          @keyup.enter="handleSearch"
          style="flex: 1;"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch" size="large">搜索</el-button>
      </div>
      
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
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { OfficeBuilding, MapLocation, CoffeeCup, Reading, Calendar, User, Search, ChatDotRound, Promotion, Setting, ArrowRight, Food, Coffee, Collection, Star, Notebook, Monitor } from '@element-plus/icons-vue'
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
    Setting,
    ArrowRight,
    Food,
    Coffee,
    Collection,
    Star,
    Notebook,
    Monitor
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
    
    // 个性化推荐相关
    const recommendations = ref([])
    
    // 加载个性化推荐
    const loadRecommendations = async () => {
      try {
        // 从 localStorage 获取当前用户ID
        const currentUserStr = localStorage.getItem('currentUser')
        let userId = null
        if (currentUserStr) {
          try {
            const currentUser = JSON.parse(currentUserStr)
            userId = currentUser.userId
          } catch (e) {
            console.error('解析用户信息失败:', e)
          }
        }
        
        // 调用后端API获取推荐
        const response = await fetch(`http://localhost:8080/api/recommendations?userId=${userId || ''}`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json'
          }
        })
        
        if (response.ok) {
          const data = await response.json()
          console.log('📥 后端返回的推荐数据:', data)
          
          // 验证数据有效性
          if (data && data.length > 0) {
            // 检查第一个元素的category是否有效
            const firstItem = data[0]
            if (!firstItem.category || firstItem.category === '' || firstItem.category === 'GENERAL_SEARCH') {
              console.warn('⚠️ 后端返回的category无效，使用默认推荐')
              loadDefaultRecommendations()
              return
            }
            
            // 数据有效，使用后端数据
            recommendations.value = data.slice(0, 3)
            console.log('✅ 加载个性化推荐:', recommendations.value)
          } else {
            console.log('📭 后端返回空数据，使用默认推荐')
            loadDefaultRecommendations()
          }
        } else {
          console.warn('⚠️ 获取推荐失败，使用默认推荐')
          loadDefaultRecommendations()
        }
      } catch (error) {
        console.error('❌ 加载推荐内容失败:', error)
        loadDefaultRecommendations()
      }
    }
    
    // 默认推荐（当没有用户数据时）
    const loadDefaultRecommendations = () => {
      recommendations.value = [
        {
          title: '热门食堂推荐',
          description: '旦苑、南苑等校园美食等你来探索',
          category: 'FACILITY_CANTEEN',
          targetPage: 'facility',
          filterType: '食堂',  // 设施类型筛选
          searchKeyword: '食堂'
        },
        {
          title: '近期校园活动',
          description: '精彩讲座、比赛不容错过',
          category: 'EVENT',
          targetPage: 'event',
          filterType: null,
          searchKeyword: ''
        },
        {
          title: '图书馆自习室',
          description: '安静舒适的学习环境',
          category: 'FACILITY_LIBRARY',
          targetPage: 'facility',
          filterType: '图书馆',  // 设施类型筛选
          searchKeyword: '图书馆'
        }
      ]
    }
    
    // 处理推荐点击
    const handleRecommendClick = (item) => {
      // 构建导航数据，包含筛选信息
      const navData = {
        targetPage: item.targetPage,
        filterType: item.filterType || null,  // 设施类型筛选（如'食堂'）
        searchKeyword: item.searchKeyword || null
      }
      
      // 将导航数据存储到 sessionStorage
      sessionStorage.setItem('recommendNavData', JSON.stringify(navData))
      
      // 跳转到对应页面
      emit('navigate', item.targetPage)
      
      ElMessage.success(`正在为您打开${getCategoryName(item.category)}...`)
    }
    
    // 获取类别图标
    const getCategoryIcon = (category) => {
      const iconMap = {
        'FACILITY_CANTEEN': 'Food',
        'FACILITY_CAFE': 'Coffee',
        'FACILITY_LIBRARY': 'Reading',
        'FACILITY_STUDY_ROOM': 'Notebook',
        'FACILITY_LAB': 'Monitor',
        'BUILDING': 'OfficeBuilding',
        'CAMPUS': 'MapLocation',
        'COURSE': 'Collection',
        'TEACHER': 'User',
        'EVENT': 'Calendar',
        'AI_SQL': 'ChatDotRound',
        'GENERAL_SEARCH': 'Search'
      }
      return iconMap[category] || 'Star'
    }
    
    // 获取类别颜色
    const getCategoryColor = (category) => {
      const colorMap = {
        'FACILITY_CANTEEN': '#E6A23C',
        'FACILITY_CAFE': '#909399',
        'FACILITY_LIBRARY': '#409EFF',
        'FACILITY_STUDY_ROOM': '#67C23A',
        'FACILITY_LAB': '#9C27B0',
        'BUILDING': '#67C23A',
        'CAMPUS': '#F56C6C',
        'COURSE': '#9C27B0',
        'TEACHER': '#00BCD4',
        'EVENT': '#FF5722',
        'AI_SQL': '#67C23A',
        'GENERAL_SEARCH': '#409EFF'
      }
      return colorMap[category] || '#409EFF'
    }
    
    // 获取类别标签类型
    const getCategoryTagType = (category) => {
      const typeMap = {
        'FACILITY_CANTEEN': 'warning',
        'FACILITY_CAFE': 'info',
        'FACILITY_LIBRARY': 'primary',
        'FACILITY_STUDY_ROOM': 'success',
        'FACILITY_LAB': '',
        'BUILDING': 'success',
        'CAMPUS': 'danger',
        'COURSE': '',
        'TEACHER': 'info',
        'EVENT': 'warning',
        'AI_SQL': 'success',
        'GENERAL_SEARCH': 'primary'
      }
      return typeMap[category] || ''
    }
    
    // 获取类别名称
    const getCategoryName = (category) => {
      const nameMap = {
        'FACILITY_CANTEEN': '食堂',
        'FACILITY_CAFE': '咖啡厅',
        'FACILITY_LIBRARY': '图书馆',
        'FACILITY_STUDY_ROOM': '自习室',
        'FACILITY_LAB': '实验室',
        'BUILDING': '建筑',
        'CAMPUS': '校区',
        'COURSE': '课程',
        'TEACHER': '教师',
        'EVENT': '活动',
        'AI_SQL': '智能问答',
        'GENERAL_SEARCH': '校园搜索'
      }
      return nameMap[category] || '推荐'
    }
    
    // 图片轮播相关
    const currentBgIndex = ref(0)
    const backgrounds = [
      '/images/carousel/fudan-campus-1.png',  // 替换为您的图片路径
      '/images/carousel/fudan-campus-2.png',
      '/images/carousel/fudan-campus-3.png',
      '/images/carousel/fudan-campus-4.png'
    ]
    
    // 定时切换背景图片
    let bgInterval = null
    const startCarousel = () => {
      bgInterval = setInterval(() => {
        currentBgIndex.value = (currentBgIndex.value + 1) % backgrounds.length
      }, 5000) // 每5秒切换一次
    }
    
    const stopCarousel = () => {
      if (bgInterval) {
        clearInterval(bgInterval)
      }
    }

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
        building_name: '所属建筑',
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
        building_type: '建筑类型',
        
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
      } else {
        ElMessage.warning('暂不支持该类型的详情查看')
      }
    }

    onMounted(() => {
      loadStats()
      loadRecommendations() // 加载推荐内容
      startCarousel() // 启动轮播
    })
    
    onUnmounted(() => {
      stopCarousel() // 清理定时器
    })

    return {
      searchKeyword,
      searchResults,
      nlQuestion,
      nlLoading,
      nlResult,
      isAdmin,
      stats,
      recommendations,
      currentBgIndex,
      backgrounds,
      handleNavigate,
      handleSearch,
      handleNLQuery,
      handleRecommendClick,
      getCategoryIcon,
      getCategoryColor,
      getCategoryTagType,
      getCategoryName,
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
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
  overflow-y: hidden; /* 禁止上下滚动 */
}

/* 卡片间距 */
.home > .el-card,
.home > .el-row {
  margin-bottom: 20px;
}

.home > .el-card:last-child,
.home > .el-row:last-child {
  margin-bottom: 0;
}

/* 欢迎卡片 - 轮播背景 */
.welcome-card {
  position: relative;
  overflow: hidden;
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.15);
  margin-bottom: 24px;
  height: 280px;
  border: none;
  transition: all 0.3s ease;
}

.welcome-card:hover {
  box-shadow: 0 12px 40px rgba(102, 126, 234, 0.25);
  transform: translateY(-2px);
}

.carousel-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.carousel-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  opacity: 0;
  transition: opacity 1s ease-in-out;
}

.carousel-bg.active {
  opacity: 1;
}

.carousel-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, 
    rgba(102, 126, 234, 0.75) 0%, 
    rgba(118, 75, 162, 0.75) 50%,
    rgba(240, 147, 251, 0.7) 100%);
  z-index: 1;
}

.welcome-content {
  position: absolute;
  z-index: 2;
  height: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 50px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.title-section h2 {
  margin: 0 0 12px 0;
  font-size: 36px;
  font-weight: 700;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #ffffff 0%, #f0f4ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.title-section p {
  margin: 0;
  font-size: 18px;
  opacity: 0.95;
  color: white;
  font-weight: 500;
  letter-spacing: 1px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 16px;
  border: none;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.1);
  height: 100%;
}

.stat-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 12px 28px rgba(102, 126, 234, 0.25);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
  font-weight: 500;
}

.feature-card {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 20px;
  border: none;
  background: linear-gradient(135deg, #ffffff 0%, #fafbfc 100%);
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.1);
  padding: 32px 24px;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.feature-card .el-icon {
  margin-bottom: 8px;
}

.feature-card:hover {
  transform: translateY(-10px) scale(1.03);
  box-shadow: 0 16px 40px rgba(102, 126, 234, 0.25);
  background: linear-gradient(135deg, #f0f4ff 0%, #e8eeff 100%);
}

.admin-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.admin-card:hover {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
}

.admin-card h3 {
  color: white !important;
}

.feature-card h3 {
  margin: 16px 0 10px 0;
  font-size: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 700;
}

.feature-card p {
  color: #666;
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
}

/* 推荐卡片样式 */
.recommend-card {
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.1);
  border: none;
  background: linear-gradient(135deg, #ffffff 0%, #fafbfc 100%);
}

.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.recommend-item {
  display: flex;
  align-items: center;
  padding: 18px;
  border-radius: 12px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #e4e7ed;
}

.recommend-item:hover {
  transform: translateX(8px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.18);
  border-color: #667eea;
  background: linear-gradient(135deg, #f0f4ff 0%, #ffffff 100%);
}

.recommend-icon {
  flex-shrink: 0;
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: white;
  box-shadow: 0 2px 12px rgba(102, 126, 234, 0.15);
  margin-right: 16px;
}

.recommend-content {
  flex: 1;
  min-width: 0;
}

.recommend-content h4 {
  margin: 0 0 6px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.recommend-content p {
  margin: 0 0 10px 0;
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
}

.recommend-arrow {
  flex-shrink: 0;
  margin-left: 12px;
  color: #c0c4cc;
  transition: all 0.3s ease;
}

.recommend-item:hover .recommend-arrow {
  color: #667eea;
  transform: translateX(4px);
}

h4 {
  margin-top: 0;
  color: #303133;
}

/* 全局卡片样式优化 */
.el-card {
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.08);
  border: none;
  transition: all 0.3s ease;
}

.el-card:hover {
  box-shadow: 0 6px 24px rgba(102, 126, 234, 0.15);
}

/* 输入框样式优化 */
.el-input__wrapper {
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.08);
  transition: all 0.3s ease;
}

.el-input__wrapper:hover {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.el-input__wrapper.is-focus {
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.2);
}

/* 按钮样式优化 */
.el-button--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  color: white !important;
}

.el-button--primary:hover {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
  transform: translateY(-2px);
  color: white !important;
}

.el-button--success {
  background: linear-gradient(135deg, #409eff 0%, #f557db 100%);
  border: none;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.3);
  color: white !important;
}

.el-button--success:hover {
  background: linear-gradient(135deg, #f5576c 0%, #f093fb 100%);
  box-shadow: 0 6px 16px rgba(245, 87, 108, 0.4);
  transform: translateY(-2px);
  color: white !important;
}

/* 表格样式优化 */
.el-table {
  border-radius: 12px;
  overflow: hidden;
}

.el-table th {
  background: linear-gradient(135deg, #f8f9fa 0%, #f0f4ff 100%) !important;
  color: #303133;
  font-weight: 600;
}

.el-table tr:hover > td {
  background-color: #f0f4ff !important;
}

/* Alert样式优化 */
.el-alert {
  border-radius: 12px;
  border: none;
}

/* Tag样式优化 */
.el-tag {
  border-radius: 8px;
  border: none;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .home {
    padding: 16px;
  }
  
  .welcome-card {
    height: 220px;
  }
  
  .title-section h2 {
    font-size: 28px;
  }
  
  .title-section p {
    font-size: 16px;
  }
  
  .stat-value {
    font-size: 28px;
  }
  
  .feature-card {
    padding: 24px 16px;
  }
}
</style>
