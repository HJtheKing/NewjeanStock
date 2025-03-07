import axios from "axios";

// const BASE_URL = "http://10.13.13.2:8080";
const BASE_URL = "https://j11a502.p.ssafy.io" + "/api";

// 토큰을 헤더에 넣지 않고, api요청 시
const axiosApi = (url, options) => {
  const instance = axios.create({
    baseURL: url,
    ...options,
    withCredentials: true,
  });
  return instance;
};

// 토큰을 헤더에 넣지 않고, 프로필 사진 관련 api요청 시
const axiosImgApi = (url, options) => {
  const instance = axios.create({
    baseURL: url,
    headers: {
      "Content-Type": "multipart/form-data",
    },
    ...options,
    withCredentials: true,
  });
  return instance;
};

// 토큰을 헤더에 넣어서 api요청 시
const axiosAuthApi = (url, options) => {
  const accessToken =
    "Bearer eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJjaGlja2Vuc3RvY2siLCJzdWIiOiIxIiwicm9sZSI6Im1lbWJlciIsImV4cCI6MTcyODM2NjMwNH0.YzLk9RYjeYMmkAQLsW1QSMSaMmaor2O46WmR-N-NYZpJBJ4fKGtrXS_-oRxchn4QJlUKuYbSdpic09-TpZ7H9w";
  const instance = axios.create({
    baseURL: url,
    headers: { Authorization: accessToken },
    ...options,
    withCredentials: true,
  });
  return instance;
};

export const defaultInstance = axiosApi(BASE_URL);
export const imgInstance = axiosImgApi(BASE_URL);
export const authInstance = axiosAuthApi(BASE_URL);
