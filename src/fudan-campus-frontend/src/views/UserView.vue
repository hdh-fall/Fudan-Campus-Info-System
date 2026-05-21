<template>
  <div class="user-view">
    <h2>👤 个人中心</h2>
    
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>📋 个人信息</span>
            </div>
          </template>
          
          <div v-if="currentUser" class="user-info">
            <p><strong>👤 姓名：</strong>{{ currentUser.name }}</p>
            <p><strong>🆔 用户名：</strong>{{ currentUser.username }}</p>
            <p><strong>🎓 年级：</strong>{{ currentUser.grade || '未设置' }}</p>
            <p><strong>🏛️ 院系：</strong>{{ getDepartmentName(currentUser.departmentId) }}</p>
            <p><strong>🎭 角色：</strong>
              <el-tag size="small" :type="currentUser.role === 'admin' ? 'danger' : 'success'">
                {{ currentUser.role === 'admin' ? '管理员' : '普通用户' }}
              </el-tag>
            </p>
            <p><strong>📧 邮箱：</strong>{{ currentUser.email || '未设置' }}</p>
            <p><strong>📱 电话：</strong>{{ currentUser.phone || '未设置' }}</p>
          </div>
          <el-empty v-else description="暂无用户信息" :image-size="80" />
          
          <el-divider />
          
          <div class="action-buttons">
            <el-button type="primary" @click="showSwitchAccountDialog" style="width: 100%;">
              🔄 切换账号
            </el-button>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>📜 查询历史</span>
              <el-tag size="small" type="info">{{ queryHistory.length }} 条记录</el-tag>
            </div>
          </template>
          
          <el-empty v-if="queryHistory.length === 0 && !loading" description="暂无查询历史" :image-size="100" />
          
          <el-table v-else :data="queryHistory" style="width: 100%" v-loading="loading" stripe>
            <el-table-column prop="question" label="问题" min-width="200" />
            <el-table-column prop="category" label="类别" width="100">
              <template #default="scope">
                <el-tag size="small" type="primary">{{ formatCategory(scope.row.category) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="queryTime" label="查询时间" width="180">
              <template #default="scope">
                {{ formatDateTime(scope.row.queryTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="resultSummary" label="结果摘要" min-width="150" />
          </el-table>
        </el-card>
        
        <el-card style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span>📊 热门查询类别统计</span>
            </div>
          </template>
          
          <el-empty v-if="popularCategories.length === 0" description="暂无统计数据" :image-size="80" />
          
          <el-table v-else :data="popularCategories" style="width: 100%" stripe>
            <el-table-column prop="0" label="类别" min-width="150">
              <template #default="scope">
                <el-tag size="small" type="success">{{ formatCategory(scope.row[0]) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="1" label="查询次数" width="120" align="center">
              <template #default="scope">
                <strong>{{ scope.row[1] }}</strong>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 切换账号对话框 -->
    <el-dialog v-model="switchAccountVisible" title="切换账号" width="500px">
      <el-alert
        title="演示模式：快速切换到预设的演示账号"
        type="info"
        :closable="false"
        style="margin-bottom: 20px;"
      />
      <el-table :data="demoAccounts" @row-click="switchToAccount" style="cursor: pointer;">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="scope">
            <el-tag size="small" :type="scope.row.role === 'admin' ? 'danger' : 'success'">
              {{ scope.row.role === 'admin' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { userAPI, queryRecordAPI, departmentAPI } from '../api'
import { ElMessage } from 'element-plus'

export default {
  name: 'UserView',
  setup() {
    const currentUser = ref(null)
    const queryHistory = ref([])
    const popularCategories = ref([])
    const loading = ref(false)
    
    // 切换账号
    const switchAccountVisible = ref(false)
    const demoAccounts = ref([])
    
    // 院系列表
    const departments = ref([])

    // 加载用户列表
    const loadUsers = async () => {
      try {
        const users = await userAPI.getAllUsers()
        // 转换数据格式，添加描述信息
        demoAccounts.value = users.map(user => {
          let description = ''
          if (user.role === 'admin') {
            description = '管理员账号'
          } else if (user.grade) {
            description = `${user.grade}学生`
          } else {
            description = '用户账号'
          }
          return {
            userId: user.userId,
            username: user.username,
            name: user.name,
            role: user.role,
            grade: user.grade,
            departmentId: user.department?.departmentId || null,
            email: user.email,
            phone: user.phone,
            description: description
          }
        })
      } catch (error) {
        console.error('加载用户列表失败:', error)
        ElMessage.error('加载用户列表失败')
      }
    }

    // 初始化默认账号
    const initDefaultAccount = () => {
      const savedUser = localStorage.getItem('currentUser')
      if (savedUser) {
        currentUser.value = JSON.parse(savedUser)
      } else if (demoAccounts.value.length > 0) {
        // 默认使用第一个用户
        currentUser.value = demoAccounts.value[0]
        localStorage.setItem('currentUser', JSON.stringify(currentUser.value))
      }
      loadQueryHistory()
      loadPopularCategories()
    }

    const loadDepartments = async () => {
      try {
        departments.value = await departmentAPI.getAllDepartments()
      } catch (error) {
        console.error('加载院系列表失败:', error)
      }
    }

    const loadQueryHistory = async () => {
      if (!currentUser.value) return
      
      loading.value = true
      try {
        const data = await queryRecordAPI.getQueryHistory(currentUser.value.userId)
        queryHistory.value = data
      } catch (error) {
        console.error('加载查询历史失败:', error)
        ElMessage.error('加载查询历史失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }

    const loadPopularCategories = async () => {
      try {
        const data = await queryRecordAPI.getPopularCategories()
        popularCategories.value = data
      } catch (error) {
        console.error('加载统计数据失败:', error)
      }
    }

    // 显示切换账号对话框
    const showSwitchAccountDialog = () => {
      switchAccountVisible.value = true
    }
    
    // 切换到指定账号
    const switchToAccount = (account) => {
      currentUser.value = {
        userId: account.userId,
        username: account.username,
        name: account.name,
        role: account.role,
        grade: account.grade,
        departmentId: account.departmentId,
        email: account.email,
        phone: account.phone
      }
      localStorage.setItem('currentUser', JSON.stringify(currentUser.value))
      switchAccountVisible.value = false
      ElMessage.success(`已切换到账号：${account.name}`)
      
      // 重新加载该账号的数据
      loadQueryHistory()
      loadPopularCategories()
    }

    const formatDateTime = (datetime) => {
      if (!datetime) return ''
      const date = new Date(datetime)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
    
    // 格式化类别名称
    const formatCategory = (category) => {
      const categoryMap = {
        'FACILITY_CANTEEN': '食堂',
        'FACILITY_CAFE': '咖啡厅',
        'FACILITY_LIBRARY': '图书馆',
        'FACILITY_SPORTS': '体育设施',
        'FACILITY_LAB': '实验室',
        'BUILDING': '建筑',
        'CAMPUS': '校区',
        'COURSE': '课程',
        'TEACHER': '教师',
        'EVENT': '活动',
        'GENERAL_SEARCH': '搜索'
      }
      return categoryMap[category] || category || '其他'
    }
    
    // 获取院系名称
    const getDepartmentName = (departmentId) => {
      if (!departmentId) return '未设置'
      const dept = departments.value.find(d => d.departmentId === departmentId)
      return dept ? dept.name : '未设置'
    }

    onMounted(async () => {
      await loadUsers()
      await loadDepartments()
      initDefaultAccount()
    })

    return {
      loading,
      currentUser,
      queryHistory,
      popularCategories,
      switchAccountVisible,
      demoAccounts,
      showSwitchAccountDialog,
      switchToAccount,
      formatDateTime,
      formatCategory,
      getDepartmentName
    }
  }
}
</script>

<style scoped>
.user-view {
  max-width: 1200px;
  margin: 0 auto;
}

h2 {
  color: #409EFF;
  margin-bottom: 20px;
  font-size: 26px;
  font-weight: bold;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

/* 用户信息样式 */
.user-info p {
  margin: 12px 0;
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
}

.user-info strong {
  color: #303133;
  font-weight: 600;
}

/* 操作按钮区域 */
.action-buttons {
  margin-top: 15px;
}

/* 表格样式优化 */
:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  color: #303133;
  font-weight: bold;
  font-size: 14px;
}

:deep(.el-table td) {
  color: #606266;
  font-size: 14px;
}
</style>
