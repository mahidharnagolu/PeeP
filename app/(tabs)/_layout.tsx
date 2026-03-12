import { Tabs } from 'expo-router';
import { Theme } from '@/constants/Colors';
import { Home, Bell, User } from 'lucide-react-native';

export default function TabLayout() {
    return (
        <Tabs
            screenOptions={{
                headerShown: false,
                tabBarStyle: {
                    backgroundColor: '#000000',
                    borderTopColor: '#1A1A1A',
                    borderTopWidth: 0.5,
                    height: 85,
                    paddingBottom: 25,
                    paddingTop: 12,
                },
                tabBarActiveTintColor: '#FFFFFF',
                tabBarInactiveTintColor: '#666666',
                tabBarShowLabel: false,
            }}
        >
            <Tabs.Screen
                name="index"
                options={{
                    title: 'Home',
                    tabBarIcon: ({ color, focused }) => (
                        <Home
                            color={color}
                            size={26}
                            strokeWidth={focused ? 2.5 : 1.8}
                            fill={focused ? color : 'transparent'}
                        />
                    ),
                }}
            />
            <Tabs.Screen
                name="notifications"
                options={{
                    title: 'Notifications',
                    tabBarIcon: ({ color, focused }) => (
                        <Bell
                            color={color}
                            size={26}
                            strokeWidth={focused ? 2.5 : 1.8}
                            fill={focused ? color : 'transparent'}
                        />
                    ),
                }}
            />
            <Tabs.Screen
                name="profile"
                options={{
                    title: 'Profile',
                    tabBarIcon: ({ color, focused }) => (
                        <User
                            color={color}
                            size={26}
                            strokeWidth={focused ? 2.5 : 1.8}
                        />
                    ),
                }}
            />
        </Tabs>
    );
}
