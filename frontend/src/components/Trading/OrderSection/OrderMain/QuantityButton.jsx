import React from "react";

const QuantityButton = ({ percent, onClickPercent, disabled }) => {
  const onClick = () => {
    onClickPercent(percent);
  };
  return (
    <button
      className="bg-yellow3 rounded font-semibold text-gray-500 hover:bg-yellow-200 hover:text-black"
      onClick={onClick}
      disabled={disabled}
    >
      {percent}%
    </button>
  );
};

export default QuantityButton;
