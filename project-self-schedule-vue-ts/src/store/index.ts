import { createStore } from 'vuex'
import auth from './modules/auth'
import focus from './modules/focus'
import task from './modules/task'
import sidebar from './modules/sidebar'

export interface RootState {
  auth: typeof auth.state
  focus: typeof focus.state
  task: typeof task.state
  sidebar: typeof sidebar.state
}

export default createStore<RootState>({
  modules: {
    auth,
    focus,
    task,
    sidebar
  }
})
