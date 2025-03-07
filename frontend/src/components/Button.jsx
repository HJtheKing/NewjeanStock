import React from "react";

const Button = ({ text, type, onClick, color }) => {
  return (
    <div>
      <button
        onClick={onClick}
        className={`${color ? "bg-" + color : "bg-yellow2"} px-9 py-3 font-bold rounded-lg hover:bg-yellow-300` }
      >
        {text}
      </button>
    </div>
  );
};

export default Button;
