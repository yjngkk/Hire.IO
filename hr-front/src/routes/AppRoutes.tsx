import React from "react";
import { Routes, Route } from "react-router-dom";
import { Layout } from "@/components/layout/Layout";
import { Dashboard } from "@/pages/Dashboard";
import { CreateJob } from "@/pages/CreateJob";
import { EditJob } from "@/pages/EditJob";
import { Jobs } from "@/pages/Jobs";
import { Applications } from "@/pages/Applications";
import { CreateCandidature } from "@/pages/CreateCandidature";
import { Tests } from "@/pages/Tests";
import { Interviews } from "@/pages/Interviews";
import { Onboarding } from "@/pages/Onboarding";
import { Settings } from "@/pages/Settings";
import QCMTests from "@/pages/QCMTests";
import NotFound from "@/pages/NotFound";
import { LandingPage } from "@/pages/LandingPage";
import ProtectedRoute from "@/components/layout/ProtectedRoute";
import { useAuth } from "@/KeycloakProvider"; // 👈
import PublicLayout from "@/components/layout/PublicLayout";

const AppRoutes: React.FC = () => {
  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
       <Route 
          path="/create-candidature/:encryptedId?" 
          element={
            <PublicLayout>
              <CreateCandidature />
            </PublicLayout>
          } 
        />
        <Route path="/qcm-tests/:accessToken" element={<QCMTests />} />
      <Route
        path="/*"
        element={
          <ProtectedRoute>
            <Layout>
              <Routes>
                <Route path="/dashboard" element={<Dashboard />} />
                <Route path="/create-job" element={<CreateJob />} />
                {/* <Route path="/create-candidature" element={<CreateCandidature />} /> */}
                <Route path="/edit-job/:jobId" element={<EditJob />} />
                <Route path="/jobs" element={<Jobs />} />
                <Route path="/applications" element={<Applications />} />
                <Route path="/tests" element={<Tests />} />
                <Route path="/interviews" element={<Interviews />} />
                <Route path="/onboarding" element={<Onboarding />} />
                <Route path="/settings" element={<Settings />} />
               <Route path="*" element={<NotFound />} />
              </Routes>
            </Layout>
          </ProtectedRoute>
        }
      />
    </Routes>
  );
};

export default AppRoutes;
