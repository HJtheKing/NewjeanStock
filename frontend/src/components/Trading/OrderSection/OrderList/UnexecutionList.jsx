import React, { useEffect } from "react";
import { useState } from "react";
import useLoginUserInfoStore from "../../../../store/useLoginUserInfoStore.js";
import useCompetitionInfoStore from "../../../../store/useCompetitionInfoStore.js";
import { defaultInstance } from "../../../../api/axios.jsx";
import {
  삼성전자,
  삼성전기,
  SK하이닉스,
  셀리드,
  한미반도체,
  NAVER,
  카카오,
  HLB,
  랩지노믹스,
  실리콘투,
} from "./../../../../assets/companylogo/index.js";

const companyLogos = {
  삼성전자: 삼성전자,
  삼성전기: 삼성전기,
  SK하이닉스: SK하이닉스,
  셀리드: 셀리드,
  한미반도체: 한미반도체,
  NAVER: NAVER,
  카카오: 카카오,
  HLB: HLB,
  랩지노믹스: 랩지노믹스,
  실리콘투: 실리콘투,
};

// 미체결 리스트 요청
// const unExecutionList = [
//   {
//     companyName: "카카오",
//     price: "39000",
//     volume: "2",
//     status: "매도",
//     created_at: "2024-08-14T12:54:20.1125",
//   },
//   {
//     companyName: "삼성전자",
//     price: "75000",
//     volume: "4",
//     status: "매수",
//     created_at: "2024-08-14T10:03:30.0007",
//   },
// ];

const UnexecutionList = () => {
  const [unexecutionList, setUnexcutionList] = useState([]);
  const [selectedOrder, setSelectedOrder] = useState(null); // 삭제할 주문 선택
  const [showDeleteModal, setShowDeleteModal] = useState(false); // 모달 상태 관리
  // const accountId = useLoginUserInfoStore(
  //   (state) => state.loginUserInfo.isLogined
  // );
  // null 이라서 에러

  // 로그인 유저의 accountId, memberId 가져오기
  const { accountId, loginId: memberId } = useLoginUserInfoStore(
    (state) => state.loginUserInfo
  );

  // competitionId 가져오기
  const { competitionId } = useCompetitionInfoStore(
    (state) => state.competitionInfo
  );

  // 미체결 내역 불러오기
  useEffect(() => {
    if (accountId) {
      getUnexecutionList(accountId);
    }
  }, [accountId]);

  // 미체결 내역 API 호출
  const getUnexecutionList = async (accountId) => {
    try {
      const response = await defaultInstance.get(
        `/account/v2/${accountId}/unexecution`
      );
      setUnexcutionList(response.data.unexecution);
    } catch (error) {
      console.error("미체결 내역 조회 실패:", error);
    }
  };

  // 주문 취소 API 요청
  const cancelOrder = async (orderId) => {
    try {
      const response = await defaultInstance.post(
        `/account/v2/cancel/stock/${orderId}`,
        {
          memberId, // 상태에서 가져온 loginId를 memberId로 사용
          accountId, // 상태에서 가져온 accountId
          competitionId, // 상태에서 가져온 competitionId
        }
      );

      if (response.data.status === "SUCCESS") {
        alert("주문이 성공적으로 취소되었습니다.");
        setUnexcutionList(
          unexecutionList.filter((order) => order.orderId !== orderId)
        ); // 취소된 주문 리스트에서 제거
      } else {
        alert("주문 취소에 실패했습니다.");
      }
    } catch (error) {
      console.error("주문 취소 실패:", error);
    }
  };

  // 모달 열기
  const openDeleteModal = (order) => {
    setSelectedOrder(order);
    setShowDeleteModal(true);
  };

  // 모달 닫기
  const closeDeleteModal = () => {
    setShowDeleteModal(false);
    setSelectedOrder(null);
  };

  return (
    <div>
      <div className="font-bold test-lg mb-4">미체결</div>
      {unexecutionList.length > 0 ? (
        unexecutionList.map((unExecution, index) => (
          <div
            className="flex gap-x-4 justify-between items-center mb-4"
            key={index}
          >
            <img
              src={companyLogos[unExecution.companyName]}
              alt={`${unExecution.companyName} 로고`}
              className="w-8 h-8 rounded-full"
            />
            <div className="flex-1">
              <div className="font-semibold">{unExecution.companyName}</div>
              <div className="flex gap-2 font-bold text-gray-400">
                <div
                  className={`text-xs ${
                    unExecution.status === "매수"
                      ? "text-red-500"
                      : "text-blue-500"
                  }`}
                >
                  {unExecution.status}
                </div>
                <div className="font-semibold text-xs">
                  {parseInt(unExecution.price).toLocaleString()}원
                </div>
                <div className="font-semibold text-xs">
                  {unExecution.volume}주
                </div>
              </div>
            </div>
            <button onClick={() => openDeleteModal(unExecution)}>⨉</button>
          </div>
        ))
      ) : (
        <div>미체결 내역이 없습니다.</div>
      )}

      {/* 삭제 확인 모달 */}
      {showDeleteModal && selectedOrder && (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
          <div className="bg-white p-6 rounded shadow-md text-center">
            <h2 className="text-xl font-bold mb-4">주문을 삭제하시겠습니까?</h2>
            <div className="flex justify-evenly">
              <button
                className="bg-red-500 text-white py-2 px-4 rounded"
                onClick={() => {
                  cancelOrder(selectedOrder.orderId);
                  closeDeleteModal();
                }}
              >
                삭제
              </button>
              <button
                className="bg-gray-300 py-2 px-4 rounded"
                onClick={closeDeleteModal}
              >
                취소
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default UnexecutionList;
