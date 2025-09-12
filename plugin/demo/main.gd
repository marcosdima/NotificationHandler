extends Control

@export var icon: Texture2D
@onready var scroll: ScrollContainer = $ScrollContainer

var _plugin_name = "NotificationHandler"
var _channel_id = "news_channel"
var _small_icon = "small_icon.png"
var _android_plugin

func _ready():
	call_deferred("_init_sizes")
	self.plugin_exists()
	
	# Save image in user.
	if !Engine.is_editor_hint():
		var small_icon_path = "user://" + _small_icon
		var text = load(icon.resource_path) as Texture2D
		var img = text.get_image()
		img.save_png(small_icon_path)


func _init_sizes():
	var _size = get_parent_area_size()
	set_size(_size)
	scroll.set_size(_size)
	self.plugin_exists()


func plugin_exists() -> void:
	if Engine.has_singleton(_plugin_name):
		_android_plugin = Engine.get_singleton(_plugin_name)
	else:
		printerr("Couldn't find plugin " + _plugin_name)


func _on_hello_pressed() -> void:
	if _android_plugin:
		_android_plugin.echo("Hello World!")


func _on_build_pressed() -> void:
	if _android_plugin:
		var channel_data = {
			"channelId": _channel_id,
			"name": "News",
			"description": "Notifications about news",
			"importance": 4
		}
		var test_array = JSON.stringify([channel_data])
		_android_plugin.setup(test_array)


func _on_simple_pressed() -> void:
	if _android_plugin:
		var title = "Hello"
		var content = "This is a test notification"
		var res = _android_plugin.triggerNotification(
			_channel_id,
			title,
			content,
			_small_icon
		)
		print("Result: ", res)


func _on_permissions_pressed() -> void:
	if _android_plugin:
		_android_plugin.requestPostNotificationsPermissions()


func _on_exit_pressed() -> void:
	get_tree().quit()
