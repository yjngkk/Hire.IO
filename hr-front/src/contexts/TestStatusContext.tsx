import React, { createContext, useContext, useState, ReactNode } from 'react';

interface TestStatusContextType {
  testStarted: boolean;
  setTestStarted: React.Dispatch<React.SetStateAction<boolean>>;
  testSubmitted: boolean;
  setTestSubmitted: React.Dispatch<React.SetStateAction<boolean>>;
}

const TestStatusContext = createContext<TestStatusContextType | undefined>(undefined);

export const TestStatusProvider = ({ children }: { children: ReactNode }) => {
  const [testStarted, setTestStarted] = useState<boolean>(false);
  const [testSubmitted, setTestSubmitted] = useState<boolean>(false);

  return (
    <TestStatusContext.Provider value={{ testStarted, setTestStarted, testSubmitted, setTestSubmitted }}>
      {children}
    </TestStatusContext.Provider>
  );
};

export const useTestStatus = () => {
  const context = useContext(TestStatusContext);
  if (context === undefined) {
    throw new Error('useTestStatus must be used within a TestStatusProvider');
  }
  return context;
}; 