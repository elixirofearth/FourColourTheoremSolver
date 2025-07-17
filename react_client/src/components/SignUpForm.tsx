import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import styles from "../styles/SignUpForm.module.css";

const SignUpForm: React.FC = () => {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
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
      const response = await fetch(`${apiHost}/api/v1/auth/register`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
        body: JSON.stringify({ name, email, password }),
      });

      if (response.ok) {
        const data = await response.json();
        console.log("Sign-up successful:", data);
        navigate("/login");
      } else {
        const data = await response.json();
        setError(data.error);
      }
    } catch (error) {
      console.error("Error during sign-up:", error);
      setError("An error occurred during sign-up");
    }
  };

  return (
    <div className={styles.signupContainer}>
      <form onSubmit={handleSubmit} className={styles.signupForm}>
        <img
          src="/logo.png"
          alt="Cartoon Logo"
          width={100}
          height={100}
          className={styles.logo}
        />
        <h2 className={`${styles.formTitle} text-lg md:text-xl lg:text-2xl`}>
          Create Your Account
        </h2>

        <div className={styles.inputGroup}>
          <label
            htmlFor="name"
            className={`${styles.label} text-sm md:text-base`}
          >
            Name
          </label>
          <input
            type="text"
            id="name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className={`${styles.inputField} text-sm md:text-base`}
            required
          />
        </div>

        <div className={styles.inputGroup}>
          <label
            htmlFor="email"
            className={`${styles.label} text-sm md:text-base`}
          >
            Email
          </label>
          <input
            type="email"
            id="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
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
          Sign Up
        </button>
        <p className={`${styles.loginLink} text-xs md:text-sm`}>
          Already have an account? <Link to="/login">Login</Link>
        </p>
      </form>
    </div>
  );
};

export default SignUpForm;
