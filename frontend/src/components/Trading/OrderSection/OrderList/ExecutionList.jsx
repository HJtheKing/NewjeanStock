import React from "react";
import { useEffect, useState } from "react";
import { defaultInstance } from "../../../../api/axios.jsx";
import useLoginUserInfoStore from "../../../../store/useLoginUserInfoStore.js";

const handleTime = (strDate) => {
  const date = new Date(strDate);
  const hour = ("0" + date.getHours()).slice(-2);
  const minute = ("0" + date.getMinutes()).slice(-2);
  const second = ("0" + date.getSeconds()).slice(-2);
  return `${hour}:${minute}:${second}`;
};

const ExecutionList = () => {
  // 체결 리스트를 요청 API (accountId를 받아왔다 치고..)

  // const executionList2 = [
  //   {
  //     companyName: "카카오",
  //     price: 36300,
  //     volume: "5",
  //     status: "매수체결",
  //     created_at: "2024-08-14T12:54:20.1125",
  //   },
  //   {
  //     companyName: "삼성전자",
  //     price: 75200,
  //     volume: "12",
  //     status: "매도체결",
  //     created_at: "2024-08-14T10:03:30.0007",
  //   },
  //   {
  //     companyName: "카카오",
  //     price: 36400,
  //     volume: "1",
  //     status: "매수체결",
  //     created_at: "2024-08-14T10:01:20.1125",
  //   },
  // ];

  // 일단 임의의 값 지정

  const [executionList, setExcutionList] = useState([]);
  // const accountId = useLoginUserInfoStore(
  //   (state) => state.loginUserInfo.isLogined
  // );
  // null 이라서 에러
  const accountId = useLoginUserInfoStore(
    (state) => state.loginUserInfo.accountId
  );

  // 체결 내역 불러오기
  useEffect(() => {
    if (accountId) {
      getExecutionList(accountId);
    }
  }, [accountId]);

  // API 요청: 체결 내역 가져오기
  const getExecutionList = async (accountId) => {
    try {
      const response = await defaultInstance.get(
        `/account/${accountId}/execution`
      );
      setExecutionList(response.data.execution);
    } catch (error) {
      console.error("체결 내역 조회 실패:", error);
      alert(
        `체결 내역 가져오기 실패: ${error.response ? error.response.data.message : error.message}`
      );
    }
  };

  return (
    <div>
      <div className="font-bold text-lg mb-4">체결 내역</div>
      {executionList.length > 0 ? (
        executionList.map((execution, index) => (
          <div
            className="flex gap-x-4 justify-between items-center mb-4"
            key={index}
          >
            <div className="flex-col text-right">
              <div className="text-sm">{handleTime(execution.created_at)}</div>
            </div>
            <div className="flex-1">
              <div className="font-semibold">{execution.companyName}</div>
              <div className="flex gap-2 font-bold text-gray-400">
                <div
                  className={`text-xs ${execution.status.substring(0, 2) === "매수" ? "text-red-500" : "text-blue-500"}`}
                >
                  {execution.status.substring(0, 2)}
                </div>
                <div className="font-semibold text-xs">
                  {parseInt(execution.price).toLocaleString()}원
                </div>
                <div className="font-semibold text-xs">
                  {execution.volume}주
                </div>
              </div>
            </div>
            <div>
              <div>
                {(execution.price * execution.volume).toLocaleString()}원
              </div>
            </div>
          </div>
        ))
      ) : (
        <div>체결 내역이 없습니다.</div>
      )}
    </div>
  );
};

export default ExecutionList;
