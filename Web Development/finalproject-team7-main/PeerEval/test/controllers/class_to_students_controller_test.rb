require "test_helper"

class ClassToStudentsControllerTest < ActionDispatch::IntegrationTest
  setup do
    @class_to_student = class_to_students(:one)
  end

  test "should get index" do
    get class_to_students_url
    assert_response :success
  end

  test "should get new" do
    get new_class_to_student_url
    assert_response :success
  end

  test "should create class_to_student" do
    assert_difference("ClassToStudent.count") do
      post class_to_students_url, params: { class_to_student: { class_id: @class_to_student.class_id, student_id: @class_to_student.student_id } }
    end

    assert_redirected_to class_to_student_url(ClassToStudent.last)
  end

  test "should show class_to_student" do
    get class_to_student_url(@class_to_student)
    assert_response :success
  end

  test "should get edit" do
    get edit_class_to_student_url(@class_to_student)
    assert_response :success
  end

  test "should update class_to_student" do
    patch class_to_student_url(@class_to_student), params: { class_to_student: { class_id: @class_to_student.class_id, student_id: @class_to_student.student_id } }
    assert_redirected_to class_to_student_url(@class_to_student)
  end

  test "should destroy class_to_student" do
    assert_difference("ClassToStudent.count", -1) do
      delete class_to_student_url(@class_to_student)
    end

    assert_redirected_to class_to_students_url
  end
end
