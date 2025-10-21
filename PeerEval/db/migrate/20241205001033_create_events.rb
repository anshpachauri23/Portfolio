class CreateEvents < ActiveRecord::Migration[7.2]
  def change
    create_table :events do |t|
      t.string :ename
      t.integer :course_id

      t.timestamps
    end
  end
end
