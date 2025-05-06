import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useState, useEffect } from 'react';
import './Navbar.css';

function Navbar() {
    const navigate = useNavigate();
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [loading, setLoading] = useState(true);

    const checkAuth = async () => {
        try {
            const response = await axios.get('http://localhost:8080/api/auth/check', {
                withCredentials: true
            });
            setIsAuthenticated(response.data === 'Authenticated');
        } catch (error) {
            setIsAuthenticated(false);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        checkAuth();
    }, []);

    const handleLogout = async () => {
        try {
            await axios.post('http://localhost:8080/api/auth/logout', {}, { withCredentials: true });
            setIsAuthenticated(false);
            navigate('/login');
        } catch (error) {
            console.error('Logout failed:', error);
        }
    };

    if (loading) {
        return null;
    }

    return (
        <nav className="navbar">
            <Link to="/home">Home</Link>
            {!isAuthenticated && <Link to="/register">Register</Link>}
            {!isAuthenticated && <Link to="/login">Login</Link>}
            <Link to="/posts">Posts</Link>
            <Link to="/messages">Messages</Link>
            <Link to="/profile/1">Profile</Link>
            {isAuthenticated && <button className="logout-button" onClick={handleLogout}>Log Out</button>}
        </nav>
    );
}

export default Navbar;