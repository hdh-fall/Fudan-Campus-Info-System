<template>
  <div id="app">
    <el-container>
      <el-header>
        <div class="header-content">
          <h1>复旦校园百事通</h1>
          <el-menu mode="horizontal" :default-active="activeMenu" @select="handleMenuSelect">
            <el-menu-item index="home">首页</el-menu-item>
            <el-menu-item index="campus">校区建筑</el-menu-item>
            <el-menu-item index="facility">校园设施</el-menu-item>
            <el-menu-item index="course">课程教师</el-menu-item>
            <el-menu-item index="event">校园活动</el-menu-item>
            <el-menu-item index="analytics">数据分析</el-menu-item>
            <el-menu-item index="user">个人中心</el-menu-item>
            <el-menu-item v-if="isAdmin" index="admin">管理后台</el-menu-item>
          </el-menu>
        </div>
      </el-header>
      
      <el-main>
        <component :is="currentComponent" @navigate="navigateTo" />
      </el-main>
      
      <el-footer>
        <p>© 2026 复旦大学校园百事通系统 - 数据库课程设计项目</p>
      </el-footer>
    </el-container>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import HomeView from './views/HomeView.vue'
import CampusView from './views/CampusView.vue'
import FacilityView from './views/FacilityView.vue'
import CourseView from './views/CourseView.vue'
import EventView from './views/EventView.vue'
import AnalyticsView from './views/AnalyticsView.vue'
import UserView from './views/UserView.vue'
import AdminView from './views/AdminView.vue'

export default {
  name: 'App',
  components: {
    HomeView,
    CampusView,
    FacilityView,
    CourseView,
    EventView,
    AnalyticsView,
    UserView,
    AdminView
  },
  setup() {
    const activeMenu = ref('home')
    
    // 获取当前用户信息
    const getCurrentUser = () => {
      const savedUser = localStorage.getItem('currentUser')
      return savedUser ? JSON.parse(savedUser) : null
    }
    
    // 检查是否为管理员
    const isAdmin = computed(() => {
      const user = getCurrentUser()
      return user && user.role === 'admin'
    })
    
    const componentMap = {
      home: 'HomeView',
      campus: 'CampusView',
      facility: 'FacilityView',
      course: 'CourseView',
      event: 'EventView',
      analytics: 'AnalyticsView',
      user: 'UserView',
      admin: 'AdminView'
    }
    
    const currentComponent = computed(() => {
      return componentMap[activeMenu.value] || 'HomeView'
    })
    
    const handleMenuSelect = (index) => {
      // 如果访问管理后台但不是管理员，阻止跳转
      if (index === 'admin' && !isAdmin.value) {
        ElMessage.warning('只有管理员才能访问管理后台')
        return
      }
      activeMenu.value = index
    }
    
    // 提供全局方法供子组件调用
    const navigateTo = (page) => {
      activeMenu.value = page
    }
    
    return {
      activeMenu,
      currentComponent,
      handleMenuSelect,
      navigateTo,
      isAdmin
    }
  }
}
</script>

<style>
#app {
  font-family: 'Microsoft YaHei', Arial, sans-serif;
  min-height: 100vh;
}

.el-header {
  background-color: #409EFF;
  color: white;
  padding: 0;
  height: auto !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
}

.header-content h1 {
  margin: 0;
  padding: 15px 20px 10px 20px;
  font-size: 24px;
  font-weight: bold;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
}

.el-menu {
  border-bottom: none !important;
  background-color: transparent !important;
  padding: 0 20px 10px 20px;
}

.el-menu-item {
  color: white !important;
  font-size: 15px;
  font-weight: 500;
  height: 40px;
  line-height: 40px;
  border-radius: 4px;
  margin: 0 2px;
}

.el-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.25) !important;
  color: white !important;
}

.el-menu-item.is-active {
  background-color: rgba(255, 255, 255, 0.35) !important;
  color: white !important;
  font-weight: bold;
}

.el-main {
  min-height: calc(100vh - 160px);
  background-color: #f5f7fa;
  padding: 20px;
}

.el-footer {
  background-color: #303133;
  color: white;
  text-align: center;
  line-height: 60px;
  font-size: 14px;
}

/* 确保内容不被遮挡 */
.campus-view,
.facility-view,
.course-view,
.event-view,
.user-view,
.admin-view,
.home {
  margin-top: 0;
  padding-top: 0;
}
</style>
