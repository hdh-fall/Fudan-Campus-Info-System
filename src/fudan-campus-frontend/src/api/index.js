import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

// 校区相关API
export const campusAPI = {
  getAllCampuses: () => api.get('/campuses'),
  getCampusById: (id) => api.get(`/campuses/${id}`)
}

// 建筑相关API
export const buildingAPI = {
  getBuildingsByCampus: (campusId) => api.get(`/buildings/campus/${campusId}`),
  searchBuildings: (keyword) => api.get('/buildings/search', { params: { keyword } }),
  getBuildingById: (id) => api.get(`/buildings/${id}`),
  createBuilding: (building) => api.post('/buildings', building),
  updateBuilding: (id, building) => api.put(`/buildings/${id}`, building),
  deleteBuilding: (id) => api.delete(`/buildings/${id}`)
}

// 设施相关API
export const facilityAPI = {
  getFacilitiesByBuilding: (buildingId) => api.get(`/facilities/building/${buildingId}`),
  getFacilitiesByType: (type) => api.get(`/facilities/type/${type}`),
  searchFacilities: (keyword) => api.get('/facilities/search', { params: { keyword } }),
  getFacilityById: (id) => api.get(`/facilities/${id}`),
  createFacility: (facility) => api.post('/facilities', facility),
  updateFacility: (id, facility) => api.put(`/facilities/${id}`, facility),
  deleteFacility: (id) => api.delete(`/facilities/${id}`)
}

// 课程相关API
export const courseAPI = {
  getAllCourses: () => api.get('/courses'),
  getAllCoursesWithTeachers: () => api.get('/courses-with-teachers'),
  getCoursesByDepartment: (departmentId) => api.get(`/courses/department/${departmentId}`),
  searchCourses: (keyword) => api.get('/courses/search', { params: { keyword } }),
  getCourseById: (id) => api.get(`/courses/${id}`),
  createCourse: (course) => api.post('/courses', course),
  updateCourse: (id, course) => api.put(`/courses/${id}`, course),
  deleteCourse: (id) => api.delete(`/courses/${id}`)
}

// 教师相关API
export const teacherAPI = {
  getAllTeachers: () => api.get('/teachers'),
  getTeachersByDepartment: (departmentId) => api.get(`/teachers/department/${departmentId}`),
  searchTeachers: (keyword) => api.get('/teachers/search', { params: { keyword } }),
  getTeacherById: (id) => api.get(`/teachers/${id}`),
  getCourseRelationsByTeacher: (teacherId) => api.get(`/teachers/${teacherId}/course-relations`),
  createTeacher: (teacher) => api.post('/teachers', teacher),
  updateTeacher: (id, teacher) => api.put(`/teachers/${id}`, teacher),
  deleteTeacher: (id) => api.delete(`/teachers/${id}`)
}

// 活动相关API
export const eventAPI = {
  getAllEvents: () => api.get('/events'),
  getUpcomingEvents: (days = 7) => api.get('/events/upcoming', { params: { days } }),
  getEventsByCampus: (campusId) => api.get(`/events/campus/${campusId}`),
  searchEvents: (keyword) => api.get('/events/search', { params: { keyword } }),
  getEventById: (id) => api.get(`/events/${id}`),
  createEvent: (event) => api.post('/events', event),
  updateEvent: (id, event) => api.put(`/events/${id}`, event),
  deleteEvent: (id) => api.delete(`/events/${id}`)
}

// 用户相关API
export const userAPI = {
  getAllUsers: () => api.get('/users'),
  getUserByUsername: (username) => api.get(`/users/username/${username}`),
  getUserById: (id) => api.get(`/users/${id}`),
  createUser: (user) => api.post('/users', user),
  deleteUser: (id) => api.delete(`/users/${id}`)
}

// 查询记录相关API
export const queryRecordAPI = {
  saveQueryRecord: (record) => api.post('/query-records', record),
  getQueryHistory: (userId) => api.get(`/query-records/user/${userId}`),
  getPopularCategories: () => api.get('/query-records/popular-categories')
}

// 院系相关API
export const departmentAPI = {
  getAllDepartments: () => api.get('/departments')
}

// 课程-教师关联相关API
export const courseTeacherAPI = {
  // 获取所有课程-教师关联记录
  getAllCourseTeachers: () => api.get('/course-teachers'),
  
  // 根据课程ID获取授课教师列表
  getTeachersByCourse: (courseId) => api.get(`/courses/${courseId}/teachers`),
  
  // 根据教师ID获取授课课程关联列表（包含学期、角色等信息）
  getCourseRelationsByTeacher: (teacherId) => api.get(`/teachers/${teacherId}/course-relations`),
  
  // 创建课程-教师关联
  createCourseTeacher: (courseTeacher) => api.post('/course-teachers', courseTeacher),
  
  // 更新课程-教师关联
  updateCourseTeacher: (courseId, teacherId, semester, courseTeacher) => 
    api.put(`/course-teachers/${courseId}/${teacherId}/${semester}`, courseTeacher),
  
  // 删除课程-教师关联
  deleteCourseTeacher: (courseId, teacherId, semester) => 
    api.delete(`/course-teachers/${courseId}/${teacherId}/${semester}`)
}

export default api
