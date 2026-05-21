<template>
  <div class="event-view">
    <h2>🎉 校园活动查询</h2>
    
    <el-card>
      <template #header>
        <div class="card-header">
          <span>📅 活动列表</span>
          <el-tag size="small" type="danger">{{ events.length }} 个活动</el-tag>
        </div>
      </template>
      
      <el-radio-group v-model="timeFilter" @change="loadEvents" style="margin-bottom: 20px;">
        <el-radio-button label="upcoming">
          <el-icon><Calendar /></el-icon>
          近期活动
        </el-radio-button>
        <el-radio-button label="all">
          <el-icon><List /></el-icon>
          全部活动
        </el-radio-button>
      </el-radio-group>

      <el-input
        v-model="searchKeyword"
        placeholder="请输入关键词，例如：讲座、比赛、晚会..."
        clearable
        @keyup.enter="handleSearch"
        style="margin-bottom: 20px;"
        size="large"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </template>
      </el-input>

      <el-empty v-if="events.length === 0 && !loading" description="暂无活动数据" :image-size="120" />

      <el-table v-else :data="events" style="width: 100%" v-loading="loading" stripe>
        <el-table-column prop="name" label="活动名称" min-width="180" />
        <el-table-column prop="eventTime" label="活动时间" width="160">
          <template #default="scope">
            <el-tag size="small" type="info">{{ formatDateTime(scope.row.eventTime) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="locationDesc" label="地点" min-width="120" />
        <el-table-column prop="organizer" label="主办方" min-width="150" />
        <el-table-column prop="category" label="类别" width="100">
          <template #default="scope">
            <el-tag size="small" type="success">{{ scope.row.category || '其他' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewDetail(scope.row)">
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="活动详情" width="50%">
      <div v-if="currentEvent" class="detail-content">
        <p><strong>🎯 活动名称：</strong>{{ currentEvent.name }}</p>
        <p><strong>📅 活动时间：</strong>{{ formatDateTime(currentEvent.eventTime) }}</p>
        <p><strong>📍 活动地点：</strong>{{ currentEvent.locationDesc || '未指定' }}</p>
        <p><strong>🏢 主办方：</strong>{{ currentEvent.organizer || '未指定' }}</p>
        <p><strong>🏷️ 类别：</strong>{{ currentEvent.category || '其他' }}</p>
        <p><strong>📝 描述：</strong>{{ currentEvent.description || '无' }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { Search, View, Calendar, List } from '@element-plus/icons-vue'
import { eventAPI } from '../api'
import { ElMessage } from 'element-plus'

export default {
  name: 'EventView',
  components: {
    Search,
    View,
    Calendar,
    List
  },
  setup() {
    const events = ref([])
    const timeFilter = ref('upcoming')
    const searchKeyword = ref('')
    const loading = ref(false)
    const dialogVisible = ref(false)
    const currentEvent = ref(null)

    const loadEvents = async () => {
      loading.value = true
      try {
        console.log('加载活动数据，过滤器:', timeFilter.value)
        if (timeFilter.value === 'upcoming') {
          const data = await eventAPI.getUpcomingEvents(15)
          console.log('近期活动（未来15天）:', data)
          events.value = data
        } else {
          // 这里简化处理，实际应该有个getAll接口
          const data = await eventAPI.getUpcomingEvents(365)
          console.log('全部活动:', data)
          events.value = data
        }
        
        if (events.value.length === 0) {
          ElMessage.info('暂无活动数据')
        }
      } catch (error) {
        console.error('加载活动失败:', error)
        ElMessage.error('加载活动失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }

    const handleSearch = async () => {
      if (!searchKeyword.value.trim()) {
        loadEvents()
        return
      }
      
      loading.value = true
      try {
        console.log('搜索活动:', searchKeyword.value)
        events.value = await eventAPI.searchEvents(searchKeyword.value)
        console.log('搜索结果:', events.value)
        
        if (events.value.length === 0) {
          ElMessage.info('未找到匹配的活动')
        }
      } catch (error) {
        console.error('搜索失败:', error)
        ElMessage.error('搜索失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }

    const viewDetail = (event) => {
      currentEvent.value = event
      dialogVisible.value = true
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

    onMounted(() => {
      loadEvents()
    })

    return {
      events,
      timeFilter,
      searchKeyword,
      loading,
      dialogVisible,
      currentEvent,
      loadEvents,
      handleSearch,
      viewDetail,
      formatDateTime
    }
  }
}
</script>

<style scoped>
.event-view {
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

/* 对话框样式 */
.detail-content p {
  margin: 12px 0;
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
}

.detail-content strong {
  color: #303133;
  font-weight: 600;
}

:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

:deep(.el-radio-button) {
  font-size: 14px;
}
</style>
