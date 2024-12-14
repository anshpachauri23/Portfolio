class AddCourseIdToHomeworks < ActiveRecord::Migration[7.2]
  def change
    add_column :homeworks, :course_id, :integer
  end
end
