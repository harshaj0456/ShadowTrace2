import { useState, useEffect } from 'react'
import Sidebar from './components/Sidebar'
import Dashboard from './pages/Dashboard'
import SpreadSim from './pages/SpreadSim'
import RiskScoring from './pages/RiskScoring'
import RemovalPlan from './pages/RemovalPlan'
import CustomSim from './pages/CustomSim'
import './App.css'

export default function App() {
  const [page, setPage] = useState('dashboard')

  return (
    <div className="app-layout">
      <Sidebar page={page} setPage={setPage} />
      <main className="app-main">
        {page === 'dashboard'  && <Dashboard />}
        {page === 'spread'     && <SpreadSim />}
        {page === 'scoring'    && <RiskScoring />}
        {page === 'removal'    && <RemovalPlan />}
        {page === 'custom'     && <CustomSim />}
      </main>
    </div>
  )
}