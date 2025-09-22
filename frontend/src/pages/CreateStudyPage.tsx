import { useCreateStudy } from "../hooks/useCreatStudy";

function CreateStudyPage() {
  // 나중에 useCreateStudy 훅으로 상태 관리를 대체
  const { studyGroupData, isLoading, error, handleChange, handleSubmit } =
    useCreateStudy();
  return (
    <div className="container mx-auto p-6 max-w-2xl">
      <h1 className="text-3xl font-bold mb-8 text-gray-800">
        새 스터디 만들기
      </h1>
      <form
        onSubmit={handleSubmit}
        className="space-y-6 bg-white p-8 rounded-lg shadow-md"
      >
        {/* 스터디 제목 */}
        <div>
          <label
            htmlFor="title"
            className="block text-sm font-medium text-gray-700"
          >
            스터디 제목
          </label>
          <input
            type="text"
            id="title"
            name="title"
            value={studyGroupData.title}
            onChange={handleChange}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
            required
          />
        </div>

        {/* 모집 인원 */}
        <div>
          <label
            htmlFor="capacity"
            className="block text-sm font-medium text-gray-700"
          >
            모집 인원 (본인 포함)
          </label>
          <input
            type="number"
            id="capacity"
            name="capacity"
            value={studyGroupData.capacity}
            onChange={handleChange}
            min="2"
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
            required
          />
        </div>

        {/* 모집 마감일 */}
        <div>
          <label
            htmlFor="deadline"
            className="block text-sm font-medium text-gray-700"
          >
            모집 마감일
          </label>
          <input
            type="datetime-local"
            id="deadline"
            name="deadline"
            value={studyGroupData.deadline}
            onChange={handleChange}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
            required
          />
        </div>

        {/* 모집 방식 */}
        <div>
          <label className="block text-sm font-medium text-gray-700">
            모집 방식
          </label>
          <div className="mt-2 space-x-4">
            <label className="inline-flex items-center">
              <input
                type="radio"
                name="policy"
                value="AUTO"
                className="form-radio"
                defaultChecked
              />
              <span className="ml-2">선착순</span>
            </label>
            <label className="inline-flex items-center">
              <input
                type="radio"
                name="policy"
                value="APPROVAL"
                className="form-radio"
              />
              <span className="ml-2">승인제</span>
            </label>
          </div>
        </div>

        {error && <p className="text-sm text-red-600 text-center">{error}</p>}

        {/* 생성 버튼 */}
        <div className="pt-4">
          <button
            type="submit"
            className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
          >
            {isLoading ? "생성 중..." : "스터디 생성하기"}
          </button>
        </div>
      </form>
    </div>
  );
}

export default CreateStudyPage;
