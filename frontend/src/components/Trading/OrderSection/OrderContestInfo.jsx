import React from "react";
import useCompetitionInfoStore from "../../../store/useCompetitionInfoStore";

const contestInfo = {
  title: "정기 2차 대회",
  startDate: "24. 07. 22",
  endDate: "24. 07. 26",
};

const OrderContestInfo = () => {
  const competitionInfo = useCompetitionInfoStore(
    (state) => state.competitionInfo
  );
  return (
    <div className="px-4 pt-1">
      <div className="flex flex-col">
        <div className="font-bold">📈 {competitionInfo.title}</div>
        <div className="text-sm text-right pt-2 font-bold text-gray-500">
          <span>{competitionInfo.startAt.slice(0, 10)} ~ {competitionInfo.endAt.slice(0, 10)}</span>
        </div>
      </div>
    </div>
  );
};

export default OrderContestInfo;
