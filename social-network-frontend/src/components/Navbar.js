import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import './Navbar.css';

function Navbar() {
    const navigate = useNavigate();

    const handleLogout = async () => {
        try {
            await axios.post('http://localhost:8080/api/auth/logout', {}, { withCredentials: true });
            navigate('/login');
        } catch (error) {
            console.error('Logout failed:', error);
        }
    };

    return (
        <nav className="navbar">
            <Link to="/home">Home</Link>
            <Link to="/register">Register</Link>
            <Link to="/login">Login</Link>
            <Link to="/posts">Posts</Link>
            <Link to="/messages">Messages</Link>
            <Link to="/profile/1">Profile</Link> {/* Ajustează userId dinamic mai târziu */}
            <button onClick={handleLogout}>Logout</button>
        </nav>
    );
}

export default Navbar;