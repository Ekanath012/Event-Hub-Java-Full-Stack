import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

const common=[['/','Overview','🏠'],['/events','Discover events','🔎'],['/notices','Notices','📢']];
const student=[['/my-registrations','My registrations','🎟️'],['/certificates','Certificates','🎓']];
const organizer=[['/organizer/events','Manage events','🗂️'],['/organizer/events/new','Create event','➕']];
const staff=[['/staff/check-in','QR check-in','📷']];
const admin=[['/admin','Control center','🧭'],['/admin/catalog','Catalog','🏛️'],['/admin/notices','Manage notices','📝'],['/admin/audit','Audit logs','🛡️']];
export default function AppShell(){const{user,logout}=useAuth();const toast=useToast();const[open,setOpen]=useState(false);const nav=useNavigate();const role=user?.role;
 const links=[...common,...(role==='STUDENT'?student:[]),...(role==='ORGANIZER'||role==='ADMIN'||role==='SUPER_ADMIN'?organizer:[]),...(role==='EVENT_STAFF'||role==='ADMIN'||role==='SUPER_ADMIN'?staff:[]),...(role==='ADMIN'||role==='SUPER_ADMIN'?admin:[])];
 const signout=async()=>{try{await logout();toast.success('Signed out successfully. See you soon!');}catch{toast.info('You have been signed out.');}nav('/login')};
 return <div className="app-shell"><aside className={open?'sidebar open':'sidebar'}><div className="brand"><div className="brand-mark">E</div><div><strong>EventHub</strong><span>Campus events</span></div></div><div className="user-chip"><div className="avatar">{user?.firstName?.[0]}{user?.lastName?.[0]}</div><div><strong>{user?.firstName} {user?.lastName}</strong><span>{role}</span></div></div><nav>{links.map(([to,label,ico])=><NavLink key={to} to={to} end={to==='/'||to==='/admin'||to==='/organizer/events'} onClick={()=>setOpen(false)} className={({isActive})=>isActive?'active':''}><span className="nav-ico">{ico}</span>{label}</NavLink>)}</nav><button className="logout-link" onClick={signout}>Sign out</button></aside><main className="app-main"><header className="topbar"><button className="menu-toggle" onClick={()=>setOpen(!open)}>☰</button><div className="topbar-title"><span>EventHub</span><strong>{role==='STUDENT'?'Student workspace':role==='ORGANIZER'?'Organizer workspace':role==='EVENT_STAFF'?'Event staff workspace':'Administration workspace'}</strong></div><NavLink className="icon-link" to="/notifications">Notifications</NavLink></header><div className="content"><Outlet/></div></main></div>}
