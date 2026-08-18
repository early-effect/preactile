import { defineConfig } from "vite";
import path from "path";

export default defineConfig({
  build: {
    rollupOptions: {
      input: path.resolve(__dirname, "main.js"),
      output: { entryFileNames: "client.js" },
    },
    outDir: path.resolve(__dirname, "../../target/site/assets"),
    emptyOutDir: false,
  },
});
