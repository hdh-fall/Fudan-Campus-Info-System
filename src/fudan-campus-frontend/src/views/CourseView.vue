<template>
  <div class="course-view">
    <h2>📚 课程与教师查询</h2>
    
    <el-tabs v-model="activeTab">
      <el-tab-pane name="courses">
        <template #label>
          <span>📖 课程列表</span>
        </template>
        
        <el-card style="margin-bottom: 20px;">
          <template #header>
            <div class="card-header">
              <span>🔍 搜索课程</span>
              <el-tag size="small" type="info">{{ courses.length }} 门课程</el-tag>
            </div>
          </template>
          
          <el-input
            v-model="courseSearchKeyword"
            placeholder="请输入关键词，例如：数据库、高等数学..."
            clearable
            @keyup.enter="searchCourses"
            size="large"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button type="primary" @click="searchCourses">搜索</el-button>
            </template>
          </el-input>
        </el-card>

        <el-empty v-if="courses.length === 0 && !courseLoading" description="暂无课程数据" :image-size="120" />
        
        <el-table v-else :data="courses" style="width: 100%" v-loading="courseLoading" stripe>
          <el-table-column prop="name" label="课程名称" min-width="180" />
          <el-table-column label="开课院系" min-width="150">
            <template #default="scope">
              {{ scope.row.department?.name || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="授课教师" min-width="280">
            <template #default="scope">
              <div v-if="scope.row.teachers && scope.row.teachers.length > 0">
                <el-tooltip 
                  v-for="(teacherInfo, index) in scope.row.teachers" 
                  :key="index"
                  placement="top"
                >
                  <template #content>
                    <div>院系: {{ teacherInfo.department?.name || '-' }}</div>
                    <div>角色: {{ teacherInfo.role || '主讲' }}</div>
                  </template>
                  <el-tag 
                    size="small"
                    type="info"
                    style="margin: 2px; cursor: pointer;"
                  >
                    {{ teacherInfo.name || '未知' }}
                    <span v-if="teacherInfo.title">({{ teacherInfo.title }})</span>
                  </el-tag>
                </el-tooltip>
              </div>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="credits" label="学分" width="80" align="center">
            <template #default="scope">
              {{ scope.row.credits || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="semesterOffered" label="开课学期" width="120">
            <template #default="scope">
              {{ scope.row.semesterOffered || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="viewCourseDetail(scope.row)">
                <el-icon><View /></el-icon>
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane name="teachers">
        <template #label>
          <span>👨‍🏫 教师列表</span>
        </template>
        
        <el-card style="margin-bottom: 20px;">
          <template #header>
            <div class="card-header">
              <span>🔍 搜索教师</span>
              <el-tag size="small" type="info">{{ teachers.length }} 位教师</el-tag>
            </div>
          </template>
          
          <el-input
            v-model="teacherSearchKeyword"
            placeholder="请输入教师姓名，例如：张三、李四..."
            clearable
            @keyup.enter="searchTeachers"
            size="large"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button type="primary" @click="searchTeachers">搜索</el-button>
            </template>
          </el-input>
        </el-card>

        <el-empty v-if="teachers.length === 0 && !teacherLoading" description="暂无教师数据" :image-size="120" />
        
        <el-table v-else :data="teachers" style="width: 100%" v-loading="teacherLoading" stripe>
          <el-table-column prop="name" label="教师姓名" min-width="120" />
          <el-table-column prop="department.name" label="所属院系" min-width="150">
            <template #default="scope">
              {{ scope.row.department?.name || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="title" label="职称" width="120">
            <template #default="scope">
              <el-tag size="small" type="warning">{{ scope.row.title || '未评定' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="email" label="邮箱" min-width="180">
            <template #default="scope">
              {{ scope.row.email || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="viewTeacherCourses(scope.row)">
                <el-icon><Reading /></el-icon>
                查看授课
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 课程详情对话框 -->
    <el-dialog v-model="courseDialogVisible" title="课程详情" width="50%">
      <div v-if="currentCourse" class="detail-content">
        <p><strong>📖 课程名称：</strong>{{ currentCourse.name }}</p>
        <p><strong>🏛️ 开课院系：</strong>{{ currentCourse.department?.name || '未指定' }}</p>
        <p><strong>🎯 学分：</strong>{{ currentCourse.credits || '未指定' }}</p>
        <p><strong>📅 开课学期：</strong>{{ currentCourse.semesterOffered || '未指定' }}</p>
        <p><strong>📝 描述：</strong>{{ currentCourse.description || '无' }}</p>
      </div>
    </el-dialog>

    <!-- 教师授课对话框 -->
    <el-dialog v-model="teacherDialogVisible" title="教师授课信息" width="60%">
      <div v-if="currentTeacher" class="detail-content">
        <p><strong>👨‍🏫 教师姓名：</strong>{{ currentTeacher.name }}</p>
        <p><strong>🏛️ 所属院系：</strong>{{ currentTeacher.department?.name || '未指定' }}</p>
        <p><strong>🎖️ 职称：</strong>{{ currentTeacher.title || '未评定' }}</p>
        <p><strong>📧 邮箱：</strong>{{ currentTeacher.email || '未提供' }}</p>
        
        <h4>📚 授课课程</h4>
        <el-empty v-if="teacherCourses.length === 0" description="该教师暂无授课信息" :image-size="80" />
        <el-table v-else :data="teacherCourses" style="width: 100%" stripe>
          <el-table-column prop="courseName" label="课程名称" min-width="180" />
          <el-table-column label="开课院系" min-width="150">
            <template #default="scope">
              {{ scope.row.department?.name || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="semester" label="开课学期" width="120">
            <template #default="scope">
              <el-tag size="small" type="warning">{{ scope.row.semester }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="role" label="担任角色" width="100">
            <template #default="scope">
              <el-tag size="small" type="success">{{ scope.row.role || '主讲' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="credits" label="学分" width="80" align="center">
            <template #default="scope">
              {{ scope.row.credits || '-' }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { Search, View, Reading } from '@element-plus/icons-vue'
import { courseAPI, teacherAPI } from '../api'
import { ElMessage } from 'element-plus'

export default {
  name: 'CourseView',
  components: {
    Search,
    View,
    Reading
  },
  setup() {
    const activeTab = ref('courses')
    const courses = ref([])
    const teachers = ref([])
    const courseSearchKeyword = ref('')
    const teacherSearchKeyword = ref('')
    const courseLoading = ref(false)
    const teacherLoading = ref(false)
    const courseDialogVisible = ref(false)
    const teacherDialogVisible = ref(false)
    const currentCourse = ref(null)
    const currentTeacher = ref(null)
    const teacherCourses = ref([])

    const loadCourses = async () => {
      courseLoading.value = true
      try {
        console.log('加载课程数据...')
        const data = await courseAPI.getAllCoursesWithTeachers()
        console.log('课程数据:', data)
        courses.value = data
        
        if (data.length === 0) {
          ElMessage.info('暂无课程数据')
        }
      } catch (error) {
        console.error('加载课程失败:', error)
        ElMessage.error('加载课程失败，请稍后重试')
      } finally {
        courseLoading.value = false
      }
    }

    const loadTeachers = async () => {
      teacherLoading.value = true
      try {
        console.log('加载教师数据...')
        const data = await teacherAPI.getAllTeachers()
        console.log('教师数据:', data)
        teachers.value = data
        
        if (data.length === 0) {
          ElMessage.info('暂无教师数据')
        }
      } catch (error) {
        console.error('加载教师失败:', error)
        ElMessage.error('加载教师失败，请稍后重试')
      } finally {
        teacherLoading.value = false
      }
    }

    const searchCourses = async () => {
      if (!courseSearchKeyword.value.trim()) {
        loadCourses()
        return
      }
      
      courseLoading.value = true
      try {
        console.log('搜索课程:', courseSearchKeyword.value)
        courses.value = await courseAPI.searchCourses(courseSearchKeyword.value)
        console.log('搜索结果:', courses.value)
        
        if (courses.value.length === 0) {
          ElMessage.info('未找到匹配的课程')
        }
      } catch (error) {
        console.error('搜索失败:', error)
        ElMessage.error('搜索失败，请稍后重试')
      } finally {
        courseLoading.value = false
      }
    }

    const searchTeachers = async () => {
      if (!teacherSearchKeyword.value.trim()) {
        loadTeachers()
        return
      }
      
      teacherLoading.value = true
      try {
        console.log('搜索教师:', teacherSearchKeyword.value)
        teachers.value = await teacherAPI.searchTeachers(teacherSearchKeyword.value)
        console.log('搜索结果:', teachers.value)
        
        if (teachers.value.length === 0) {
          ElMessage.info('未找到匹配的教师')
        }
      } catch (error) {
        console.error('搜索失败:', error)
        ElMessage.error('搜索失败，请稍后重试')
      } finally {
        teacherLoading.value = false
      }
    }

    const viewCourseDetail = (course) => {
      currentCourse.value = course
      courseDialogVisible.value = true
    }

    const viewTeacherCourses = async (teacher) => {
      currentTeacher.value = teacher
      teacherDialogVisible.value = true
      
      try {
        // 获取课程-教师关联数据（包含学期信息）
        const courseRelations = await teacherAPI.getCourseRelationsByTeacher(teacher.teacherId)
        
        // 将CourseTeacher对象转换为包含课程和学期信息的格式
        teacherCourses.value = courseRelations.map(relation => ({
          courseId: relation.courseId,
          courseName: relation.course?.name || '未知课程',
          department: relation.course?.department,
          credits: relation.course?.credits,
          semester: relation.semester,
          role: relation.role
        }))
      } catch (error) {
        ElMessage.error('加载授课信息失败')
      }
    }

    onMounted(() => {
      // 检查是否有推荐导航数据
      const navDataStr = sessionStorage.getItem('recommendNavData')
      if (navDataStr) {
        try {
          const navData = JSON.parse(navDataStr)
          console.log('收到推荐导航数据:', navData)
          
          // 如果有searchKeyword，自动填充搜索框并执行搜索
          if (navData.searchKeyword) {
            courseSearchKeyword.value = navData.searchKeyword
            searchCourses()
          } else {
            loadCourses()
            loadTeachers()
          }
          
          // 清除已处理的导航数据
          sessionStorage.removeItem('recommendNavData')
        } catch (e) {
          console.error('解析导航数据失败:', e)
          loadCourses()
          loadTeachers()
        }
      } else {
        // 正常加载
        loadCourses()
        loadTeachers()
      }
      
      // 检查是否有从快速搜索传来的详情数据
      const detailData = localStorage.getItem('viewDetailData')
      if (detailData) {
        try {
          const parsed = JSON.parse(detailData)
          if (parsed.category === '课程' || parsed.category === '教师') {
            // 延迟执行，等待数据加载完成
            setTimeout(() => {
              if (parsed.category === '课程') {
                viewCourseDetail(parsed.data)
              } else {
                viewTeacherCourses(parsed.data)
              }
            }, 500)
          }
          // 清除已处理的详情数据
          localStorage.removeItem('viewDetailData')
        } catch (e) {
          console.error('解析详情数据失败:', e)
        }
      }
    })

    return {
      activeTab,
      courses,
      teachers,
      courseSearchKeyword,
      teacherSearchKeyword,
      courseLoading,
      teacherLoading,
      courseDialogVisible,
      teacherDialogVisible,
      currentCourse,
      currentTeacher,
      teacherCourses,
      searchCourses,
      searchTeachers,
      viewCourseDetail,
      viewTeacherCourses
    }
  }
}
</script>

<style scoped>
.course-view {
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

h4 {
  margin-top: 20px;
  margin-bottom: 15px;
  color: #303133;
  font-size: 16px;
  font-weight: bold;
}

:deep(.el-tabs__item) {
  font-size: 14px;
}
</style>
