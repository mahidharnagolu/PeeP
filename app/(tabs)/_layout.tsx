import { Tabs } from 'expo-router';
import { Theme } from '@/constants/Colors';
import { Home, Bell, User } from 'lucide-react-native';

export default function TabLayout() {
    return (
        <Tabs
            screenOptions={{
                headerShown: false,
                tabBarStyle: {
                    backgroundColor: Theme.colors.background,
                    borderTopColor: Theme.colors.border,
                    borderTopWidth: 1,
                    height: 80, // Taller tab bar to match wireframe proportionally
                    paddingBottom: 20,
                    paddingTop: 10,
                },
                tabBarActiveTintColor: Theme.colors.text,
                tabBarInactiveTintColor: Theme.colors.secondary,
                tabBarShowLabel: false, // Match wireframe (icons only)
            }}
        >
            <Tabs.Screen
                name="index"
                options={{
                    title: 'Home',
                    tabBarIcon: ({ color, size }) => (
                        <Home color={color} size={30} strokeWidth={2.5} />
                    ),
                }}
            />
            <Tabs.Screen
                name="notifications"
                options={{
                    title: 'Notifications',
                    tabBarIcon: ({ color, size }) => (
                        <Bell color={color} size={30} strokeWidth={2.5} />
                    ),
                }}
            />
            <Tabs.Screen
                name="profile"
                options={{
                    title: 'Profile',
                    tabBarIcon: ({ color, size }) => (
                        <User color={color} size={30} strokeWidth={2.5} />
                    ),
                }}
            />
        </Tabs>
    );
}
