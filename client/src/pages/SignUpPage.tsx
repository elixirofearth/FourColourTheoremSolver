import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAppSelector } from "../store/hooks";
import SignUpForm from "../components/SignUpForm";

export default function SignUpPage() {
  const navigate = useNavigate();
  const { isAuthenticated } = useAppSelector((state) => state.auth);

  useEffect(() => {
    // Check if user is already authenticated
    if (isAuthenticated) {
      // If authenticated, redirect to home page
      navigate("/", { replace: true });
    }
  }, [isAuthenticated, navigate]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-pink-600 via-purple-600 to-blue-500 py-12 px-4 sm:px-6 lg:px-8">
      <SignUpForm />
    </div>
  );
}
