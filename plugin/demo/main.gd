extends Control

@onready var scroll: ScrollContainer = $ScrollContainer

var _plugin_name = "NotificationHandler"
var _channel_id = "news_channel"
var _android_plugin

func _ready():
	call_deferred("_init_sizes")
	self.plugin_exists()


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
			"importance": 3
		}
		var test_array = JSON.stringify([channel_data])
		_android_plugin.setup(test_array)


func _on_simple_pressed() -> void:
	if _android_plugin:
		var channel_id = _channel_id
		var title = "Hello"
		var content = "This is a test notification"
		var icon_path = "res://icon.svg"
		
		_android_plugin.triggerNotification(channel_id, title, content, icon_path)
