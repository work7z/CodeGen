import { createListenerMiddleware } from "@reduxjs/toolkit";

// Best to define this in a separate file, to avoid importing
// from the store file into the rest of the codebase
export const listenerMiddleware = createListenerMiddleware();

export const { startListening, stopListening } = listenerMiddleware;
