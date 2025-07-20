import React from "react";
import {
  render,
  type RenderOptions,
  screen,
  fireEvent,
  waitFor,
} from "@testing-library/react";
import { Provider } from "react-redux";
import { BrowserRouter } from "react-router-dom";
import { PersistGate } from "redux-persist/integration/react";
import { store, persistor } from "../store/store";
import NotificationProvider from "../contexts/NotificationContext";

interface CustomRenderOptions extends Omit<RenderOptions, "wrapper"> {
  store?: typeof store;
}

function customRender(
  ui: React.ReactElement,
  { store: customStore = store, ...renderOptions }: CustomRenderOptions = {}
) {
  function Wrapper({ children }: { children: React.ReactNode }) {
    return (
      <Provider store={customStore}>
        <PersistGate loading={null} persistor={persistor}>
          <NotificationProvider>
            <BrowserRouter>{children}</BrowserRouter>
          </NotificationProvider>
        </PersistGate>
      </Provider>
    );
  }

  return render(ui, { wrapper: Wrapper, ...renderOptions });
}

// Re-export testing library functions
export { screen, fireEvent, waitFor };

// Override render method
export { customRender as render };
