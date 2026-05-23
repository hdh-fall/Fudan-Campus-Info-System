<template>
  <div id="app">
    <el-container>
      <el-header>
        <div class="header-content">
          <div class="header-title">
            <img src="/fudan-logo.png" alt="复旦大学校徽" class="header-logo" />
            <h1>复旦校园百事通</h1>
          </div>
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
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
    // 添加一个触发器来强制更新 isAdmin
    const userChangeTrigger = ref(0)
    
    // 获取当前用户信息
    const getCurrentUser = () => {
      const savedUser = localStorage.getItem('currentUser')
      return savedUser ? JSON.parse(savedUser) : null
    }
    
    // 检查是否为管理员
    const isAdmin = computed(() => {
      // 依赖 userChangeTrigger 来触发重新计算
      userChangeTrigger.value
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
    
    // 监听用户信息变化事件
    const handleUserChange = () => {
      userChangeTrigger.value++
      console.log('用户信息已更新，当前是否为管理员:', isAdmin.value)
    }
    
    onMounted(() => {
      window.addEventListener('userChanged', handleUserChange)
    })
    
    onUnmounted(() => {
      window.removeEventListener('userChanged', handleUserChange)
    })
    
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
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  min-height: 100vh;
}

.el-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0;
  height: auto !important;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
  position: sticky;
  top: 0;
  z-index: 1000;
  backdrop-filter: blur(10px);
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px 8px 20px;
}

.header-logo {
  width: 48px;
  height: 48px;
  object-fit: contain;
  filter: drop-shadow(0 2px 8px rgba(0, 0, 0, 0.2)) brightness(1.05);
  transition: all 0.3s ease;
}

.header-logo:hover {
  transform: rotate(5deg) scale(1.05);
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.3)) brightness(1.1);
}

.header-content h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  background: linear-gradient(135deg, #ffffff 0%, #f0f4ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 1.5px;
  text-shadow: none;
}

.el-menu {
  border-bottom: none !important;
  background-color: transparent !important;
  padding: 0 20px 12px 20px;
}

.el-menu-item {
  color: rgba(255, 255, 255, 0.9) !important;
  font-size: 14px;
  font-weight: 500;
  height: 38px;
  line-height: 38px;
  border-radius: 8px;
  margin: 0 3px;
  padding: 0 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.el-menu-item:hover {
  background: rgba(255, 255, 255, 0.2) !important;
  color: white !important;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 255, 255, 0.15);
}

.el-menu-item.is-active {
  background: rgba(255, 255, 255, 0.3) !important;
  color: white !important;
  font-weight: 600;
  box-shadow: 0 4px 16px rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(8px);
}

.el-main {
  min-height: calc(100vh - 140px);
  background: linear-gradient(180deg, #f8f9fa 0%, #ffffff 100%);
  padding: 24px;
}

.el-footer {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: rgba(255, 255, 255, 0.9);
  text-align: center;
  line-height: 50px;
  font-size: 13px;
  box-shadow: 0 -2px 12px rgba(102, 126, 234, 0.2);
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

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content h1 {
    font-size: 18px;
  }
  
  .header-logo {
    width: 40px;
    height: 40px;
  }
  
  .el-menu-item {
    font-size: 13px;
    padding: 0 12px;
  }
}
</style>
