package zerobase.weather.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import zerobase.weather.domain.Diary;
import zerobase.weather.repository.DiaryRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DiaryService {


    @Value("${openweathermap.key}")
    private String apiKey;

    private final DiaryRepository diaryRepository;

    // 생성자 (DiaryService 클래스 내부에서 diaryRepository 변수를 사용할 수 있음)
    public  DiaryService(DiaryRepository diaryRepository) {
        this.diaryRepository = diaryRepository;
    }
    public void createDiary(LocalDate date, String text) {
        // open weather map에서 데이터 받아오기
        String weatherData = getWeatherString();

        // 받아온 날씨 데이터 파싱하기
        Map<String, Object> parseWeather = parseWeather(weatherData);

        // 우리 db에 저장하기
        Diary nowDiary = new Diary();
        nowDiary.setWeather(parseWeather.get("main").toString());
        nowDiary.setIcon(parseWeather.get("icon").toString());
        nowDiary.setTemperature((Double) parseWeather.get("temp"));
        nowDiary.setText(text);
        nowDiary.setDate(date);
        diaryRepository.save(nowDiary);
    }

    private String getWeatherString() {
        // API 요청 URL 생성
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=seoul&appid=" + apiKey;

        try {
            // URL 객체 생성
            URL url = new URL(apiUrl);

            // URL을 통해 HttpURLConnection 객체 생성
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // HTTP GET 메서드 설정
            connection.setRequestMethod("GET");

            // API 응답 코드 확인
            int responseCode = connection.getResponseCode();

            // 응답 데이터를 읽어올 BufferedReader 선언
            BufferedReader br;

            // 응답 코드가 200인 경우 (성공적인 응답)
            if (responseCode == 200) {
                // HttpURLConnection에서 InputStream을 얻어와서 BufferedReader로 래핑
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                // 응답 코드가 200이 아닌 경우 (에러 응답)
                // HttpURLConnection에서 에러 스트림을 얻어와서 BufferedReader로 래핑
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            // 응답 데이터를 읽어서 StringBuilder에 추가ㅋ
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

            // StringBuilder에 저장된 응답 데이터를 문자열로 변환하여 반환
            return response.toString();

        } catch (Exception e) {
            // 예외가 발생한 경우 "failed to get response" 문자열 반환
            return "failed to get response";
        }
    }
    private Map<String, Object> parseWeather(String jsonString) {
        // JSON 파서 객체 생성
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try {
            // 주어진 JSON 문자열을 JSON 객체로 파싱
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
        } catch (ParseException e) {
            // JSON 파싱 중 오류가 발생한 경우 RuntimeException으로 감싸서 예외를 던짐
            throw new RuntimeException(e);
        }

        // 결과를 저장할 맵 객체 생성
        Map<String, Object> resultMap = new HashMap<>();

        // JSON 객체에서 "main" 키의 값(또 다른 JSON 객체)을 가져와서 resultMap에 "temp" 키로 저장
        JSONObject mainData = (JSONObject) jsonObject.get("main");
        resultMap.put("temp", mainData.get("temp"));

        // JSON 객체에서 "weather" 키의 값(또 다른 JSON 배열)을 가져와서 첫 번째 객체(또 다른 JSON 객체)를 추출
        JSONArray weatherDataArray = (JSONArray) jsonObject.get("weather");
        JSONObject weatherData = (JSONObject) weatherDataArray.get(0);

        // 추출한 첫 번째 객체에서 "main"과 "icon" 키의 값을 resultMap에 각각 "main"과 "icon" 키로 저장
        resultMap.put("main", weatherData.get("main"));
        resultMap.put("icon", weatherData.get("icon"));

        // 완성된 resultMap 반환
        return resultMap;
    }

    public List<Diary> readDiary(LocalDate date) {
        return diaryRepository.findAllByDate(date);
    }

    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
        return diaryRepository.findAllByDateBetween(startDate, endDate);
    }

    public void updateDiary(LocalDate date, String text){
       Diary nowDiary = diaryRepository.getFirstByDate(date);
       nowDiary.setText(text);
       diaryRepository.save(nowDiary);
    }
    public void deleteDiary(LocalDate date) {
        diaryRepository.deleteAllByDate(date);
    }
}

