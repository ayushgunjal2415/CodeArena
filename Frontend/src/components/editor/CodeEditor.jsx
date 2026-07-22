import { useEffect, useState } from "react";
import Editor from "@monaco-editor/react";

export default function CodeEditor({
  code,
  setCode,
  language,
  setLanguage,
  starterCode = [],
  onRun,
  onSubmit,
  isRunning,
  onEndTest,
  showEndTest = true, // Default to true to maintain backward compatibility
  customInput,
  setCustomInput,
}) {
  const [version, setVersion] = useState("-");

  // ✅ Default language as "java" if not already set
  useEffect(() => {
    if (!language) {
      setLanguage("java");
    }
  }, [language, setLanguage]);


  // ⚙️ Update editor version and code whenever the language changes
 useEffect(() => {

  if (language === "java") {

    setVersion("17.0.0");

    setCode(`import java.util.*;

public class Main {

    public static void main(String[] args) {

    }
}`);

  }

  else if (language === "cpp") {

    setVersion("10.2.0");

    setCode(`#include <iostream>
using namespace std;

int main() {

    return 0;
}`);

  }

  else if (language === "python") {

    setVersion("3.10.0");

    setCode(`# Write your code here`);

  }

  else if (language === "javascript") {

    setVersion("18.0.0");

    setCode(`function main() {

}

main();`);

  }

}, [language, starterCode]);

  return (
    <div className="flex flex-col flex-1 bg-bg h-full">
      {/* === Header Section === */}
      <div className="flex justify-between items-center px-3 py-2 bg-surface border-b border-border flex-none">
        {/* LEFT SIDE */}
        <div className="flex items-center gap-3">
          <label className="text-sm text-muted">Language:</label>

          <select
            value={language || "java"}
            onChange={(e) => setLanguage(e.target.value)}
            className="border border-border rounded px-2 py-1 bg-bg text-sm text-teal-50"
          >
            {(starterCode && starterCode.length > 0) ? (
              // Show languages from starterCode if available
              starterCode.map((s) => (
                <option key={s.language} value={s.language.toLowerCase()}>
                  {s.language}
                </option>
              ))
            ) : (
              // Fallback: Show default languages if no starterCode
              <>
                <option value="java">Java</option>
                <option value="python">Python</option>
                <option value="cpp">C++</option>
                <option value="javascript">JavaScript</option>
              </>
            )}
          </select>

          {/* RUN BUTTON */}
          <button
            onClick={onRun}
            disabled={isRunning}
            className="px-3 py-1 bg-gray-700 text-white  rounded text-sm hover:bg-gray-700"
          >
            {isRunning ? "Running..." : "Run"}
          </button>

          {/* SUBMIT BUTTON */}
          <button
            onClick={onSubmit}
            className="px-3 py-1 bg-primary text-white rounded text-sm hover:opacity-90"
          >
            Submit
          </button>

          {/* END TEST BUTTON - Only shown when showEndTest is true */}
          {showEndTest && onEndTest && (
            <button
              onClick={onEndTest}
              className="px-3 py-1 bg-red-500 text-white rounded text-sm hover:opacity-90"
            >
              End Test
            </button>
          )}
        </div>

        {/* RIGHT SIDE */}
        <p className="text-xs text-muted">
          Version:{" "}
          <span className="text-primary font-semibold">{version || "-"}</span>
        </p>
      </div>


      {/* === Monaco Editor Wrapper === */}
<div className="h-[500px] overflow-hidden">
  <Editor
    key={language}
    height="100%"
    theme="vs-dark"
    language={language || "java"}
    value={code}
    onChange={(val) => setCode(val)}
    options={{
      fontSize: 14,
      minimap: { enabled: false },
      scrollBeyondLastLine: false,
      automaticLayout: true,
      wordWrap: "on",
    }}
  />
</div>

   {/* === Custom Input Box === */}
<div className="p-2 border-t border-border bg-surface">
  <label className="text-sm text-muted block mb-1">
    Custom Input
  </label>

  <textarea
  value={customInput}
  onChange={(e) => setCustomInput(e.target.value)}
  placeholder="Enter input here..."
  className="w-full h-24 p-2 rounded bg-bg border border-border text-white resize-none"
/>
</div>
    </div>
  );
}
