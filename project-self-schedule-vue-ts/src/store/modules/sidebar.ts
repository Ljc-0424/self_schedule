export const state = () => ({
  isCollapsed: false
})

export type SidebarState = ReturnType<typeof state>

export const mutations = {
  SET_COLLAPSED(state: SidebarState, collapsed: boolean) {
    state.isCollapsed = collapsed
  },
  
  TOGGLE_COLLAPSED(state: SidebarState) {
    state.isCollapsed = !state.isCollapsed
  }
}

export const actions = {
  setCollapsed({ commit }: { commit: Function }, collapsed: boolean) {
    commit('SET_COLLAPSED', collapsed)
  },
  
  toggleCollapsed({ commit }: { commit: Function }) {
    commit('TOGGLE_COLLAPSED')
  }
}

export const getters = {
  isCollapsed: (state: SidebarState) => state.isCollapsed
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
