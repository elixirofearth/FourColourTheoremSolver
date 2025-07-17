import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import styles from "../styles/LoginForm.module.css";

const LoginForm: React.FC = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");

    const apiHost = import.meta.env.VITE_API_GATEWAY_URL;

    if (!apiHost) {
      throw new Error("API host is not defined in the environment variables");
    }

    try {
      const response = await fetch(`${apiHost}/api/v1/auth/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email: username, password: password }),
      });

      if (response.ok) {
        const data = await response.json();
        localStorage.setItem("token", data.token);
        localStorage.setItem("name", data.name);
        localStorage.setItem("userId", data.user_id);
        localStorage.setItem("email", data.email);
        console.log("Sign-in successful:", data);
        navigate("/");
      } else {
        setError("Invalid username or password");
      }
    } catch (error) {
      console.error("Error during sign-in:", error);
      setError("An error occurred during sign-in");
    }
  };

  return (
    <div className={styles.loginContainer}>
      <form onSubmit={handleSubmit} className={styles.loginForm}>
        <img
          src="/logo.png"
          alt="Cartoon Logo"
          width={100}
          height={100}
          className={styles.logo}
        />
        <h2 className={`${styles.formTitle} text-lg md:text-xl lg:text-2xl`}>
          Four-Color Map Theorem Solver
        </h2>

        <div className={styles.inputGroup}>
          <label
            htmlFor="username"
            className={`${styles.label} text-sm md:text-base`}
          >
            Email
          </label>
          <input
            type="text"
            id="username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className={`${styles.inputField} text-sm md:text-base`}
            required
          />
        </div>

        <div className={styles.inputGroup}>
          <label
            htmlFor="password"
            className={`${styles.label} text-sm md:text-base`}
          >
            Password
          </label>
          <input
            type="password"
            id="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className={`${styles.inputField} text-sm md:text-base`}
            required
          />
        </div>

        {error && <p className={`${styles.error} text-sm`}>{error}</p>}

        <button
          type="submit"
          className={`${styles.submitButton} text-sm md:text-base`}
        >
          Login
        </button>
        <p className={`${styles.signupLink} text-xs md:text-sm`}>
          Don't have an account? <Link to="/signup">Sign up</Link>
        </p>
      </form>
    </div>
  );
};

export default LoginForm;
