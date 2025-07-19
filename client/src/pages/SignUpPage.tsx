import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import SignUpForm from "../components/SignUpForm";

export default function SignUpPage() {
  const navigate = useNavigate();

  useEffect(() => {
    // Check if user is already authenticated
    const token = localStorage.getItem("token");
    if (token) {
      // If authenticated, redirect to home page
      navigate("/", { replace: true });
    }
  }, [navigate]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-pink-600 via-purple-600 to-blue-500 py-12 px-4 sm:px-6 lg:px-8">
      <SignUpForm />
    </div>
  );
}
